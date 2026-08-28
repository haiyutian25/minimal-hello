# Minimal Studio 多模块架构与核心功能开发完全指南

> 本文档详细剖析项目的多模块工程结构、MVVM 状态管理、Navigation 3 导航、Hilt 依赖注入，以及所有 UI 组件、多主题切换、推拽式侧边栏、打字机启动页的开发原理、核心实现与调参指南。
>
> **本版本已按多模块化重构后的实际代码逐项核验（核验日期：2026-08-28）**，所有参数、路径与功能描述均与当前源码一致。

---

## 目录
1. [项目整体架构与模块划分](#1-项目整体架构与模块划分)
2. [MVVM 状态管理与数据流](#2-mvvm-状态管理与数据流)
3. [Hilt 依赖注入图谱](#3-hilt-依赖注入图谱)
4. [Navigation 3 导航系统](#4-navigation-3-导航系统)
5. [设计令牌系统（core:ui）](#5-设计令牌系统coreui)
6. [顶部导航栏 (TopNavBar) 与高度调控](#6-顶部导航栏-topnavbar-与高度调控)
7. [推拽式侧边栏 (Push Canvas Sidebar) 动画架构](#7-推拽式侧边栏-push-canvas-sidebar-动画架构)
8. [底部导航栏 (BottomNavBar)](#8-底部导航栏-bottomnavbar)
9. [启动页打字机引擎 (SplashScreen)](#9-启动页打字机引擎-splashscreen)
10. [主画布工作台 (CanvasScreen)](#10-主画布工作台-canvasscreen)
11. [功能屏幕：排印工作室 / 令牌面板 / 设置页 / CSS 审查器](#11-功能屏幕排印工作室--令牌面板--设置页--css-审查器)
12. [共享工具与测试体系](#12-共享工具与测试体系)
13. [核心组件参数速查](#13-核心组件参数速查)
14. [已知局限与工程问题](#14-已知局限与工程问题)

---

## 1. 项目整体架构与模块划分

本应用基于 **Jetpack Compose**（无任何 XML 布局），采用 **多模块 + MVVM + UDF（单向数据流）** 架构。

构建基线：AGP 9.1.1、Kotlin 2.2.10、Compose BOM 2026.08.00、Navigation 3 1.1.4、Hilt 2.60.1、Room 2.7.0、compileSdk 37、minSdk 24 / targetSdk 36、JDK 21。

### 1.1 模块结构与职责

```
minimal-hello/
├── app/                          # 应用壳：单 Activity + 导航组装 + 主题应用
│   ├── MinimalHelloApplication   # @HiltAndroidApp 入口
│   └── MainActivity              # @AndroidEntryPoint：hiltViewModel() + MinimalTheme + GreetingNavHost
├── core/
│   ├── data/                     # 数据层：仓库接口/实现 + Hilt 绑定
│   │   ├── model/UserPreferences             # 领域模型（themeId + typographyChoice）
│   │   ├── repository/GreetingRepository     # 策展文案数据源（6 组语录 + 5 条说明）
│   │   ├── repository/UserPreferencesRepository  # 偏好读写（Room 支撑）
│   │   └── di/DataModule                     # @Binds 绑定
│   ├── database/                 # Room：user_preferences 单行表
│   │   ├── UserPreferencesEntity / UserPreferencesDao / AppDatabase
│   │   └── di/DatabaseModule                 # @Provides 数据库与 DAO
│   ├── navigation/               # Navigation 3 封装
│   │   └── AppNavigator                      # NavigationState 包装（navigate/replace/goBack）
│   ├── network/                  # Retrofit/OkHttp/Moshi（Hilt 提供，GreetingApi 占位契约）
│   └── ui/                       # 设计系统
│       ├── theme/CssTokens       # CssVariables 模型、12 套预设、ThemeResolver、:root 导出
│       ├── theme/Theme           # MinimalTheme + LocalCssVariables + M3 ColorScheme 映射
│       ├── theme/Type            # Material3 Typography
│       └── util/Clipboard        # Context.copyToClipboard 共享扩展
└── feature/
    └── greeting/
        ├── api/                  # 导航契约：@Serializable GreetingNavKey（Splash / Main）
        └── impl/                 # UI + ViewModel
            ├── GreetingViewModel             # @HiltViewModel，功能唯一状态源
            ├── GreetingNavHost               # NavDisplay + entryProvider 组装
            ├── MainScreen                    # 推拽侧边栏 + 4 标签 Scaffold + 审查器
            ├── screens/CanvasScreen          # 主画布工作台（原 MainActivity 内联代码抽取）
            ├── screens/{Splash,TypeStudio,Tokens,Settings}Screen
            └── components/{TopNavBar,BottomNavBar,SidebarDrawer,CssVariableInspector}
```

### 1.2 模块依赖方向（只能向下依赖）

```
app ──► feature:greeting:impl ──► feature:greeting:api
 │              │  │  │                    │
 │              │  │  └► core:navigation ──┴► Navigation3 runtime/ui
 │              │  └► core:data ──► core:database ──► Room
 │              └► core:ui
 └► core:ui
core:network（独立，Hilt 图谱在 app 汇聚）
```

> 规则：feature 之间不互相依赖；跨 feature 导航只允许依赖对方的 `:api` 模块；主题令牌放在 `core:ui` 保证所有模块可用。

---

## 2. MVVM 状态管理与数据流

### 2.1 GreetingViewModel（feature:greeting:impl）

`@HiltViewModel` 注入 `UserPreferencesRepository` 与 `GreetingRepository`，以 `StateFlow` 暴露全部功能状态：

| StateFlow | 类型 | 说明 |
| :--- | :--- | :--- |
| `currentTheme` | `CssVariables` | 由 `themeId` + `primaryOverride` 经 `combine` 合成 |
| `typographyChoice` | `AppTypographyChoice` | EDITORIAL / SANS / MONO，持久化 |
| `currentTab` | `NavigationTab` | CANVAS / TYPOGRAPHY / TOKENS / SETTINGS |
| `isSidebarOpen` / `isInspectorVisible` | `Boolean` | 瞬态 UI 状态 |
| `greetingIndex` | `Int` | 策展语录循环下标 |
| `customGreeting` | `CustomGreetingState` | 自定义问候（part1/part2/isActive） |

意图方法（View → ViewModel）：`selectTab / openSidebar / closeSidebar / toggleSidebar / showInspector / hideInspector / nextGreeting / updateCustomGreeting / selectTypography / selectTheme / overridePrimary`。

### 2.2 持久化回路（修复了旧版"重启即失忆"问题）

```
用户选择主题/排版 → ViewModel.selectTheme()/selectTypography()
    → viewModelScope.launch { repository.updateTheme()/updateTypography() }
    → Room upsert（user_preferences 单行表）
    → DAO Flow 发射 → init 中的 collect 回填 _themeId/_typographyChoice
```

- 主题解析集中在 `core:ui` 的 `ThemeResolver.fromThemeId(themeId)`（未知 ID 兜底 EditorialLight）。
- 审查器的 `--primary` 实时覆盖是**瞬态**的（`_primaryOverride`），不入库，与旧版行为一致。

### 2.3 View 层观察方式

所有屏幕通过 `collectAsState()` 观察 StateFlow，回调直接调用 VM 方法：

```kotlin
val currentTheme by viewModel.currentTheme.collectAsState()
// ...
onTabSelected = { viewModel.selectTab(it); viewModel.closeSidebar() }
```

纯瞬态动画状态（按压缩放、入场触发、复制成功标记、渲染延迟模拟值）仍留在 Composable 本地 `remember`，符合 MVVM"UI 瞬态不下沉"原则。

---

## 3. Hilt 依赖注入图谱

| 模块 | 注入内容 | 作用域 |
| :--- | :--- | :--- |
| `app` | `@HiltAndroidApp MinimalHelloApplication`、`@AndroidEntryPoint MainActivity` | — |
| `core:database` | `DatabaseModule`：`AppDatabase`（Room.databaseBuilder，`minimal-hello.db`）、`UserPreferencesDao` | Singleton |
| `core:network` | `NetworkModule`：`OkHttpClient`（BASIC 日志）、`Retrofit`（Moshi 转换器）、`GreetingApi` | Singleton |
| `core:data` | `DataModule`：`@Binds` 两个仓库实现 | Singleton |
| `feature:greeting:impl` | `@HiltViewModel GreetingViewModel` | ViewModel |

`MainActivity` 中通过 `hiltViewModel()`（`androidx.hilt.lifecycle.viewmodel.compose` 包）获取 VM，`MinimalTheme(cssVars = currentTheme)` 包裹 `GreetingNavHost`。

> `core:network` 当前无实际端点（`GreetingApi` 为空契约），仅把完整 Retrofit/OkHttp 栈接入 Hilt 图谱，未来加接口只需在 `GreetingApi` 声明方法。

---

## 4. Navigation 3 导航系统

### 4.1 导航契约（feature:greeting:api）

```kotlin
@Serializable
sealed interface GreetingNavKey : NavKey {
    @Serializable data object Splash : GreetingNavKey   // 打字机启动页
    @Serializable data object Main : GreetingNavKey     // 主界面
}
```

键实现 `androidx.navigation3.runtime.NavKey` 并标注 `@Serializable`，支持进程死亡后的状态恢复。

### 4.2 AppNavigator（core:navigation）

对 `NavBackStack<NavKey>`（Navigation 3 的 NavigationState）的 MVVM 包装：`navigate(key)` 压栈、`replace(key)` 清栈替换、`goBack()` 弹栈。`rememberAppNavigator(vararg startKeys)` 内部使用 `rememberNavBackStack`，回退栈在配置变更/进程死亡后自动恢复。

### 4.3 GreetingNavHost（feature:greeting:impl）

```kotlin
val navigator = rememberAppNavigator(GreetingNavKey.Splash)

NavDisplay(
    backStack = navigator.navigationState,
    entryProvider = entryProvider {
        entry<GreetingNavKey.Splash> {
            SplashScreen(currentTheme = currentTheme,
                onFinish = { navigator.replace(GreetingNavKey.Main) })
        }
        entry<GreetingNavKey.Main> {
            MainScreen(viewModel = viewModel,
                onReplaySplash = { navigator.replace(GreetingNavKey.Splash) })
        }
    }
)
```

- Splash 完成/跳过 → `replace(Main)`（栈内只剩 Main，系统返回键直接退出应用）。
- 设置页"重播启动页" → `replace(Splash)`。
- 侧边栏打开时的返回键由 `MainScreen` 内的 `BackHandler(enabled = isSidebarOpen)` 优先拦截（先收侧边栏，再交给导航）。

---

## 5. 设计令牌系统（core:ui）

### 5.1 CssVariables 数据模型

`core/ui/theme/CssTokens.kt` 定义 **13 个颜色令牌 + 3 个圆角令牌**，一一对应 CSS Custom Properties：`background(--bg) / foreground(--text) / card(--surface) / cardForeground / border / primary(--accent) / primaryForeground / muted(--muted-bg) / mutedForeground(--muted) / accent / accentForeground / ring / subtleSurface` + `radiusSm=10dp / radiusMd=16dp / radiusLg=24dp`。

> ⚠️ `toCssString()` 导出时 `--muted` 对应 `mutedForeground`（文字色），背景色 `muted` 导出为非标准的 `--muted-bg`。直接用于 Web 端需留意语义差异。

### 5.2 12 套预设（ProductionPalettes）

6 大家族 × 明暗双版：

| 家族 | 标志色 | 浅色底 / 深色底 |
| :--- | :--- | :--- |
| **Editorial**（经典报刊） | 浅黑 `#000000` / 深白 `#FFFFFF` | `#FAFAFA` / `#0A0A0A` |
| **Geist**（Vercel 极简） | 电光蓝 `#0070F3` | `#FFFFFF` / `#000000` |
| **Linear**（黑曜石） | Linear 紫 `#5E6AD2` | `#F7F8F9` / `#08090A` |
| **Shadcn Zinc**（冷灰工业） | 浅 `#18181B` / 深 `#FAFAFA` | `#FFFFFF` / `#09090B` |
| **Notion Warm**（暖调侘寂） | 赤陶红 `#EB5757` | `#FBFBFA` / `#191919` |
| **Braun Dieter Rams**（包豪斯） | 布劳恩信号橙 `#FF5500` | `#E8E8E3` / `#111111` |

### 5.3 主题注入与切换动画

`MinimalTheme(cssVars)` 经 `CompositionLocalProvider(LocalCssVariables provides cssVars)` 注入令牌，同时映射为 Material 3 `ColorScheme`。`MainScreen` 对背景色做 280ms `FastOutSlowInEasing` 全局插值过渡：

```kotlin
val animatedBg by animateColorAsState(
    targetValue = currentTheme.background,
    animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
    label = "bg_color"
)
```

主题家族识别采用 `themeId` 前缀匹配（`editorial/geist/linear/shadcn/notion`，兜底 `Braun`），在 CanvasScreen 中按需使用；**新增主题家族时记得同步前缀分支**。

### 5.4 共享剪贴板工具

`core/ui/util/Clipboard.kt` 提供 `Context.copyToClipboard(text, label)`，取代了旧版 6 处内联 ClipboardManager 样板代码（画布、审查器、设置页、侧边栏、令牌面板、排印工作室统一调用）。

---

## 6. 顶部导航栏 (TopNavBar) 与高度调控

位于 `feature/greeting/impl/components/TopNavBar.kt`（`ProductionTopNavBar`）。

### 6.1 结构与高度

```kotlin
Column(modifier = modifier.fillMaxWidth().background(currentTheme.background).statusBarsPadding()) {
    Row(modifier = Modifier.fillMaxWidth().height(44.dp).padding(horizontal = 10.dp)) {
        // 仅侧边栏开关(28dp 点击区域 + 28dp 纯 Menu 图标，无底色/边框/按压水波纹，testTag = "top_nav_sidebar_btn")
    }
    Box(Modifier.fillMaxWidth().height(1.dp).background(currentTheme.border)) // 1px 分割线
}
```

顶栏为**双形态**设计（`pageTitle` 参数驱动）：

- **主界面形态**（`settingsLevel == NONE`）：仅左侧侧边栏开关按钮（28dp 纯 Menu 图标，无底色/边框/水波纹，`testTag = "top_nav_sidebar_btn"`）。
- **子页面形态**（设置流程内）：左侧按钮自动变为**返回键**（ArrowBack，`testTag = "top_nav_back_btn"`，点击 → `backSettings()`），**居中显示当前页面标题**——菜单页显示 "Settings"，外观设置页显示对应菜单名 "Appearance & Themes"。

两种形态均保留底部 1px 分割线。原 Monogram 徽标、HELLO 标题、呼吸脉冲药丸、右侧主题家族徽标均已移除；设置入口在侧边栏底部，主题切换在设置页/画布快切条完成。

**粗细调整**：
- **整条栏高度**：改 `Row` 的 `height(44.dp)`——细：36~40dp；粗：56/64dp（Material 3 标准），同步微调内部按钮（28dp）。
- **分割线粗细**：改底部 `Box` 的 `height(1.dp)`——发丝线 0.5dp、加粗 2dp、整块删除则无分割线。

`.statusBarsPadding()` 保证状态栏避让。设置页 `SettingsScreen` 已移除自带的页面级顶栏（含返回按钮），全应用顶部区域统一由 `ProductionTopNavBar` 管理。

---

## 7. 推拽式侧边栏 (Push Canvas Sidebar) 动画架构

侧边栏展开时**主画布被同步推开**（非浮动蒙层），实现位于 `MainScreen.kt`。

### 7.1 双层平移动画

```kotlin
val sidebarWidth = 295.dp
val luxuryPushEasing = remember { CubicBezierEasing(0.16f, 1f, 0.3f, 1f) } // 豪华减速曲线

// 主画布位移 0→295dp；侧边栏位移 -295dp→0；均为 320ms
val pushOffset by animateDpAsState(if (isSidebarOpen) sidebarWidth else 0.dp, tween(320, easing = luxuryPushEasing))
val sidebarOffset by animateDpAsState(if (isSidebarOpen) 0.dp else -sidebarWidth, tween(320, easing = luxuryPushEasing))
// 主画布圆角 0→18dp、投影 0→14dp 同步动画
```

### 7.2 层级结构

1. 底层：295dp 位移槽内的 `AppSidebarContent`（内容自身声明 `width(300.dp)`，5dp 溢出被裁剪——调槽宽时两处需同步）。
2. 表层：被推开的主画布（`offset(pushOffset)` + 动态圆角/阴影/1dp 描边）。
3. 收回机制（3 种）：① **系统返回键/手势**（`BackHandler(enabled = isSidebarOpen)` 优先拦截，只收侧边栏不退出页面）；② **点击侧边栏以外的区域**（全透明拦截层，`testTag = "sidebar_outside_dismiss"`，无视觉遮罩；拦截层通过 `padding(start = sidebarWidth)` 在几何上**只覆盖侧边栏右侧区域**——若做成全屏，点在侧边栏非交互区域的事件未被消费时会穿透到拦截层导致误收回）；③ **点击底部设置入口**（打开设置菜单页后自动收回）。层级顺序：主画布（底）→ 外部点击拦截层（中，仅展开时存在）→ 侧边栏（顶）。

### 7.3 侧边栏内容（AppSidebarContent）

极简两段式结构：**顶部工作区头部**（34dp 衬线斜体 "H" 头像 + "Hello Studio" + PRO 徽标 + "Design Systems Lab" 副标题，无关闭按钮），中间弹性留白，**底部固定设置入口**（齿轮图标 + "Settings"，点击打开**设置菜单列表页**并自动收回侧边栏——该入口从顶栏迁移而来）。原导航组、调色板快切、快捷工具与引擎页脚均已移除；标签切换由底部导航栏承担，主题切换在设置页/画布快切条完成。

---

## 8. 底部导航栏 (BottomNavBar)

`components/BottomNavBar.kt`（`ProductionBottomNavBar`）：4 标签（CANVAS/TYPOGRAPHY/TOKENS/SETTINGS），激活态为微胶囊背景 + 颜色过渡，`.navigationBarsPadding()` 避让手势条。选中回调在 `MainScreen` 中同时关闭侧边栏并退出设置流程（`exitSettings()`，防御性保留）。**底部导航栏仅在主界面（`settingsLevel == NONE`）显示**——设置菜单页与设置页不接入底部导航；**第 4 标签（SETTINGS）内容区为空白占位**，设置功能已整体迁移至侧边栏设置流程。

---

## 9. 启动页打字机引擎 (SplashScreen)

`screens/SplashScreen.kt`，纯无状态组件（`currentTheme + onFinish`），由 Nav3 的 Splash 键承载。

核心时序（已核验）：
- **光标闪烁**：480ms `LinearEasing` 无限往复。
- **光环呼吸**：1800ms `FastOutSlowInEasing`；**自转渐变环**：12000ms `LinearEasing` 旋转。
- **打字节奏**：起始延迟 350ms → 逐字 `typingDelay` → 行间停顿 400ms → 末句停顿 850ms 后触发 `onFinish`（350ms 淡出过渡）。
- 阶段进度条与 Skip/Enter 双入口均可提前结束。

---

## 10. 主画布工作台 (CanvasScreen)

`screens/CanvasScreen.kt`——重构时从旧 `MainActivity`（1077 行）抽取的独立屏幕，观察 `GreetingViewModel`。

### 10.1 区块构成

1. **遥测头**：`DESIGN TOKENS` 徽标 + 模拟延迟药丸（`renderLatencyMs`，3~5ms 随机，主题变化时刷新）+ "Inspect CSS" 快捷入口。
2. **预设快切条**：6 家族药丸（明暗随当前主题），点击 → `viewModel.selectTheme(targetVariant)`（持久化）。
3. **英雄卡片**：6 组策展语录（`GreetingRepository.heroQuotes`，轻斜体 + 粗体双段）循环，点按弹簧缩放 0.985f（`Spring.DampingRatioMediumBouncy`），tap → `viewModel.nextGreeting()`；自定义问候激活时优先显示 `customGreeting`。
4. **交错入场动画**：卡片/眉题/双段文字/分割线/说明文字按 60/120/180/300/420/480ms 延迟级联，650~750ms `CubicBezierEasing(0.16,1,0.3,1)`。
5. **自定义问候编辑器**：`AnimatedVisibility` 展开双 `BasicTextField`（part1 衬线斜体 / part2 无衬线粗体），输入即 `viewModel.updateCustomGreeting()` 实时上屏。
6. **排版引擎选择器**：Serif / Sans / Mono 三段，→ `viewModel.selectTypography()`（持久化）。
7. **CSS Variables 速览卡**：7 行令牌（`--bg/--text/--surface/--accent/--muted/--border/--radius`）+ Copy CSS 按钮（1.8s 复制成功态）。

---

## 11. 功能屏幕：排印工作室 / 令牌面板 / 设置页 / CSS 审查器

### 11.1 排印工作室（TypeStudioScreen）
3 字体族卡片 + Italic 开关 + 双滑块：**字号 24~64sp（默认 42）**、**字距 -2.0~4.0sp（默认 -0.5）**，样张行高 = 字号 × 1.15；样张短语可复制（`copyToClipboard(label = "Typography Sample")`）。

### 11.2 令牌面板（TokensScreen）
令牌搜索过滤 + 色卡矩阵（点击复制 Hex，Toast 反馈）+ 令牌化组件演练场 + 审查器入口。

### 11.3 设置流程（菜单页 + 外观设置页）

设置采用**内容区层级导航**（遵循官方 Scaffold 模式：`topBar` 定义在 Scaffold 层持久存在，仅 `content` 区切换页面，全程不新增顶栏）。`GreetingViewModel.settingsLevel` 三级状态机：

```
NONE（正常标签）→ MENU（设置菜单列表）→ PAGE（外观设置页）
```

- **入口**：侧边栏底部 "Settings" → `openSettingsMenu()`。
- **菜单页（SettingsMenuScreen）**：当前唯一入口 **"Appearance & Themes"**（外观与主题：明暗模式、调色板、排版与 CSS 令牌），点击 → `openAppearanceSettings()`。
- **外观设置页（SettingsScreen）**：明暗双卡选择器、12 调色板列表（当前选中高亮）、3 排版引擎、CSS 导出（复制 `toCssString()`）、审查器入口、系统信息与**启动页重播**（经 `onReplaySplash` → Nav3 `replace(Splash)`）。
- **返回**：系统返回键/手势逐级回退（`BackHandler` → `backSettings()`：PAGE→MENU→NONE）。设置层级内**不显示底部导航栏**（Scaffold `bottomBar` 仅在 NONE 层级渲染），页面视觉只保留全局顶栏 + 内容区。

### 11.4 CSS 变量审查器（CssVariableInspectorSheet）
全局 `ModalBottomSheet`，双标签：
- **CSS Code**：完整 `:root` 导出 + 复制按钮（1.8s 成功态）。
- **Tokens**：9 个预设色样实时覆盖 `--primary`（`onCustomPrimarySelected` → `viewModel.overridePrimary()`，瞬态不持久化）。

---

## 12. 共享工具与测试体系

### 12.1 测试栈

| 测试 | 内容 | 说明 |
| :--- | :--- | :--- |
| `ExampleUnitTest` | 2+2 | 模板级 |
| `ExampleRobolectricTest` | 读取 `app_name` 资源 | Robolectric |
| `GreetingScreenshotTest` | Roborazzi 渲染 `TokensScreen` | 验证迁移后 UI 可组合渲染 |

- Robolectric 基线 **SDK 36**（`app/src/test/resources/robolectric.properties`），**要求 JDK 21**（SDK 36 沙盒硬性要求；SDK 37 需 Robolectric 4.17-beta，暂不采用）。
- 截图基准图生成：`gradle :app:testDebugUnitTest -Proborazzi.test.record=true`。

### 12.2 构建验证命令

```
gradle :app:compileDebugKotlin    # 全模块编译 + KSP（Room/Hilt）
gradle :app:assembleDebug         # 完整打包（需根目录 debug.keystore）
gradle :app:testDebugUnitTest     # 单元测试 + 截图测试
```

---

## 13. 核心组件参数速查

| 参数 | 值 | 位置 |
| :--- | :--- | :--- |
| 顶栏内容行高 | 44dp | TopNavBar |
| 侧边栏槽宽 / 内容宽 | 295dp / 300dp | MainScreen / SidebarDrawer |
| 推拽动画 | 320ms，CubicBezier(0.16,1,0.3,1) | MainScreen |
| 主画布推开圆角 / 投影 | 18dp / 14dp | MainScreen |
| 背景色过渡 | 280ms FastOutSlowIn | MainScreen |
| 英雄卡片按压 | 0.985f 弹簧 | CanvasScreen |
| 入场级联延迟 | 60/120/180/300/420/480ms | CanvasScreen |
| 打字机 | 起始 350ms，行间 400ms，收尾 850ms | SplashScreen |
| 光标 / 光环 / 自转环 | 480ms / 1800ms / 12000ms | SplashScreen |
| 字号 / 字距滑块 | 24~64sp / -2.0~4.0sp | TypeStudioScreen |
| 复制成功态 | 1.8s | CanvasScreen / 审查器 |
| 偏好数据库 | `minimal-hello.db`，user_preferences 单行表 | core:database |

---

## 14. 已知局限与工程问题

1. **未使用的依赖模块**：`core:network` 为空契约占位（Retrofit/OkHttp 已接入 Hilt 但无端点）。
2. **主题家族前缀兜底**：`else -> "Braun"` 分支在新增家族时可能误判，需同步维护。
3. **`toCssString()` 语义偏差**：`--muted` 导出文字色、背景色用非标准 `--muted-bg`，Web 端复用需注意。
4. **侧边栏 5dp 裁剪**：槽宽 295dp 与内容 300dp 不同步，调整时两处一起改。
5. **假遥测**：画布 "Xms" 延迟药丸为随机模拟值，非真实性能指标。
6. **截图基准**：`tokens.png` 基准图需用 `-Proborazzi.test.record=true` 生成后入库。
7. **触控目标**：顶栏部分 28dp 按钮低于 48dp 无障碍建议值。
8. **debug 密钥库**：`debug.keystore` 被 gitignore，新环境需按 README 用 keytool 生成。

---

> 本文档与多模块化重构后的源码（提交 `0.2 多模块化重构`）逐项核验一致。后续修改组件参数时，请同步更新第 13 节速查表。
