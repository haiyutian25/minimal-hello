# Minimal Studio 组件架构与核心功能开发完全指南

> 本文档详细剖析了项目中所有 UI 组件、多主题颜色模式切换、顶部导航栏高度调控、推拽式侧边栏、底部导航栏以及打字机动画启动页的开发原理、核心代码实现与调参指南。
>
> **本版本已按工作区实际代码逐项核验修订（核验日期：2026-08-28）**，所有参数、数量与功能描述均与当前源码一致。

---

## 目录
1. [项目整体架构与设计系统](#1-项目整体架构与设计系统)
2. [多颜色模式与主题切换系统开发全解](#2-多颜色模式与主题切换系统开发全解)
3. [顶部导航栏 (TopNavBar) 开发与高度精确调控机制](#3-顶部导航栏-topnavbar-开发与高度精确调控机制)
4. [推拽式侧边栏 (Push Canvas Sidebar) 动画开发架构](#4-推拽式侧边栏-push-canvas-sidebar-动画开发架构)
5. [底部导航栏 (BottomNavBar) 开发与触控适配](#5-底部导航栏-bottomnavbar-开发与触控适配)
6. [启动页打字机动态效果 (SplashScreen Engine) 开发](#6-启动页打字机动态效果-splashscreen-engine-开发)
7. [字体排印系统与三大字体族 (Typography Engine)](#7-字体排印系统与三大字体族-typography-engine)
8. [CSS 变量实时审查器与各功能屏幕实装](#8-css-变量实时审查器与各功能屏幕实装)
   - 8.1 [CSS 变量实时审查器 (CssVariableInspector.kt)](#81-css-变量实时审查器-cssvariableinspectorkt)
   - 8.2 [排印工作室 (TypeStudioScreen.kt)](#82-排印工作室-typestudioscreenkt)
   - 8.3 [设计令牌面板 (TokensScreen.kt)](#83-设计令牌面板-tokensscreenkt)
   - 8.4 [系统设置页面 (SettingsScreen.kt)](#84-系统设置页面-settingsscreenkt)
   - 8.5 [主画布交互工作台 (MainActivity.kt Canvas)](#85-主画布交互工作台-mainactivitykt-canvas)
9. [剪贴板交互、内存态状态与触控无障碍设计](#9-剪贴板交互内存态状态与触控无障碍设计)
10. [核心组件参数速查与调优指南](#10-核心组件参数速查与调优指南)
11. [已知局限与工程问题](#11-已知局限与工程问题)

---

## 1. 项目整体架构与设计系统

本应用基于 **Jetpack Compose** 现代响应式 UI 框架构建（**无任何 XML 布局文件**），遵循 **单向数据流（UDF, Unidirectional Data Flow）** 与 **编辑美学设计系统（Editorial Aesthetic Design System）**。

构建基线：AGP 9.1.1、Kotlin 2.2.10、Compose BOM 2024.09.00、minSdk 24 / targetSdk 36。

### 1.1 目录与组件职责划分
```
app/src/main/java/com/example/
├── MainActivity.kt               # 应用根容器：全局主题状态、启动页过渡、推拽画布、4 标签路由、主画布工作台
├── ui/
│   ├── components/               # 核心复用交互组件
│   │   ├── TopNavBar.kt          # 顶部导航栏（44dp 内容行、侧边栏开关、状态脉冲灯、主题徽标、设置入口）
│   │   ├── BottomNavBar.kt       # 底部导航栏（4 标签、微胶囊激活态、导航避让、颜色过渡动画）
│   │   ├── SidebarDrawer.kt      # 推拽侧边栏抽屉内容（导航、调色板快切、快捷工具、引擎信息页脚）
│   │   └── CssVariableInspector.kt # CSS 变量底栏实时审查器（CSS 代码 / 令牌色板双标签 + 主色实时覆盖）
│   ├── screens/                  # 核心功能视图
│   │   ├── SplashScreen.kt       # 动态启动页（打字机文本引擎、自转光环、阶段进度、跳过/进入）
│   │   ├── TypeStudioScreen.kt   # 排印工作室（字体族切换、样张看板、字号/字距滑块、字形样张）
│   │   ├── TokensScreen.kt       # 设计令牌面板（令牌搜索过滤、组件演练场、色卡列表、Hex 复制）
│   │   └── SettingsScreen.kt     # 外观与参数设置（明暗模式、调色板列表、排版切换、启动页重播）
│   └── theme/                    # 设计令牌与色彩系统
│       ├── CssTokens.kt          # CssVariables 数据模型、12 套预设色盘、Hex 转换、:root CSS 导出
│       ├── Color.kt / Type.kt    # Compose 原生色彩与排版支持（Type.kt 定义 Material3 Typography）
│       └── Theme.kt              # Material 3 主题适配器（MinimalTheme + CompositionLocal 注入）
```

### 1.2 应用级导航结构
`MainActivity` 通过 `AnimatedContent` 在 **启动页 (Splash)** 与 **主界面** 之间切换；主界面为 `Scaffold` + 4 标签结构（`NavigationTab.CANVAS / TYPOGRAPHY / TOKENS / SETTINGS`），配合左侧推拽式侧边栏与全局 `ModalBottomSheet` 审查器。

---

## 2. 多颜色模式与主题切换系统开发全解

项目抛弃了传统硬编码色彩的方式，引入了全套映射 CSS 规范的 **`CssVariables` 令牌架构**。

### 2.1 CSS 变量数据模型 (`CssVariables`)
在 `com/example/ui/theme/CssTokens.kt` 中定义了完整的色彩令牌映射（共 13 个颜色令牌 + 3 个圆角令牌）：

```kotlin
data class CssVariables(
    val themeId: String,
    val name: String,
    val description: String,
    val isDark: Boolean,

    // 核心色彩令牌 (对应 CSS Custom Properties)
    val background: Color,        // --bg: 画布主底色
    val foreground: Color,        // --fg / --text: 主要文字与图标色
    val card: Color,              // --card / --surface: 卡片与次级面板色
    val cardForeground: Color,    // --card-fg: 卡片上的文字色
    val border: Color,            // --border: 1px 边框与视觉分割线
    val primary: Color,           // --primary / --accent: 主交互强调色
    val primaryForeground: Color, // --primary-fg: 强调色上的反白/反黑文字
    val muted: Color,             // --muted-bg: 弱化背景色
    val mutedForeground: Color,   // --muted: 次要说明文本与弱化图标
    val accent: Color,            // 交互悬停 / 次级状态色（映射 M3 primaryContainer）
    val accentForeground: Color,  // accent 上的文字色
    val ring: Color,              // --ring: 聚焦与选中外光晕
    val subtleSurface: Color,     // 微弱色阶衬底（药丸、徽标底色）

    // 圆角令牌 (Radius Tokens)
    val radiusSm: Dp = 10.dp,
    val radiusMd: Dp = 16.dp,
    val radiusLg: Dp = 24.dp      // 核心主圆角
)
```

> ⚠️ 注意：`toCssString()` 导出时 `--muted` 对应的是 `mutedForeground`（文字色），背景色 `muted` 被导出为非标准的 `--muted-bg`。若将导出的 CSS 直接用于 Web 端，需留意这一语义差异。

### 2.2 预设的 6 大家族 × 明暗双版 = 12 套色彩模式 (`ProductionPalettes`)
系统内置 6 大现代数字产品主题家族，每家族含明/暗两个变体，共 **12 套预设**（见 `ProductionPalettes.AllPresets`）：

| 家族 | 浅色变体 | 深色变体 | 标志色 |
| :--- | :--- | :--- | :--- |
| **Editorial**（经典报刊） | `#FAFAFA` 暖纸底 / `#111111` 文字 | `#0A0A0A` 碳黑底 / `#F5F5F5` 文字 | 浅色 primary 纯黑 `#000000`，深色 primary 纯白 `#FFFFFF` |
| **Geist**（Vercel 极简） | 纯白 `#FFFFFF` / `#171717` | 深空黑 `#000000` / `#EDEDED` | 电光蓝 `#0070F3` |
| **Linear**（黑曜石） | 浅板岩 `#F7F8F9` / `#1A1B1E` | 深海蓝黑 `#08090A` / `#F2F3F5` | Linear 紫 `#5E6AD2` |
| **Shadcn Zinc**（冷灰工业） | 纯白 `#FFFFFF` / `#09090B` | 锌黑 `#09090B` / `#FAFAFA` | 反转型：浅用 `#18181B`、深用 `#FAFAFA` |
| **Notion Warm**（暖调侘寂） | 米纸 `#FBFBFA` / `#37352F` | 暖黑 `#191919` / `#EFEFEF` | 赤陶红 `#EB5757` |
| **Braun Dieter Rams**（包豪斯） | 阳极氧化铝灰 `#E8E8E3` / `#1A1A1A` | 哑光黑 `#111111` / `#F5F5F0` | 布劳恩信号橙 `#FF5500` |

所有变体统一使用 `radiusSm=10dp / radiusMd=16dp / radiusLg=24dp` 圆角令牌。

### 2.3 动态切换与全局色彩平滑过渡实现
在 `MainActivity.kt` 中维护单点主题状态：

```kotlin
var currentTheme by remember { mutableStateOf(ProductionPalettes.EditorialLight) }

// 全局背景色插值过渡，切换主题时实现柔和淡入淡出，避免刺眼闪烁
val animatedBg by animateColorAsState(
    targetValue = currentTheme.background,
    animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
    label = "bg_color"
)
```

任何组件通过回调 `onThemeChange: (CssVariables) -> Unit` 更新 `currentTheme`，`MinimalTheme` 经 `CompositionLocalProvider(LocalCssVariables provides cssVars)` 注入令牌，并同时映射为 Material 3 `ColorScheme`，所有下层子组件均基于 `currentTheme.*` 重组渲染。

主题家族识别统一采用 `themeId` 前缀匹配（该逻辑在 MainActivity、TopNavBar、SidebarDrawer、SettingsScreen 四处重复实现）：

```kotlin
val currentPresetBase = when {
    currentTheme.themeId.startsWith("editorial") -> "Editorial"
    currentTheme.themeId.startsWith("geist") -> "Geist"
    currentTheme.themeId.startsWith("linear") -> "Linear"
    currentTheme.themeId.startsWith("shadcn") -> "Shadcn"
    currentTheme.themeId.startsWith("notion") -> "Notion"
    else -> "Braun"   // 兜底分支：未来新增主题会被误判为 Braun
}
```

---

## 3. 顶部导航栏 (TopNavBar) 开发与高度精确调控机制

顶部导航栏位于 `ui/components/TopNavBar.kt`（`ProductionTopNavBar`），采用符合 Apple HIG 与 Linear 规范的紧凑排版。

### 3.1 导航栏整体高度计算与结构
导航栏由 **外层沉浸式安全区** 与 **内层固定高度行（Row）** 组合而成：

```kotlin
Column(
    modifier = modifier
        .fillMaxWidth()
        .background(currentTheme.background)
        .statusBarsPadding() // ① 自动避让 Android 顶部状态栏高度 (一般为 24~48dp)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)   // ② 核心内容行高度，精确控制在 44dp
            .padding(horizontal = 10.dp), // ③ 水平安全间距
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧：侧边栏开关(28dp) + Monogram 徽标(24dp) + HELLO 标题(12sp) + 状态脉冲灯/v1.0 药丸
        // 右侧：主题调色板徽标 (点击直达设置页) + 设置按钮(28dp)
    }

    // ④ 底部 1px 极细像素分割线
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(currentTheme.border)
    )
}
```

组件签名（`onOpenSidebar` 带默认空实现，便于复用）：

```kotlin
fun ProductionTopNavBar(
    currentTheme: CssVariables,
    onThemeChange: (CssVariables) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenInspector: () -> Unit,
    onOpenSidebar: () -> Unit = {},
    modifier: Modifier = Modifier
)
```

### 3.2 如何调整顶部导航栏的高度
若需根据业务需求自定义高度，可调整以下两处：
- **微调内容高度**：修改 `Row(modifier = Modifier.height(44.dp))`。
  - 标准精简型：`44.dp`（当前默认，紧凑科技感）
  - 标准 Material 3 规格：`56.dp` 或 `64.dp`
  - 如果调整了内容高度，内部的图标按钮（`size(28.dp)`）与字体大小（`12~13.sp`）可按比例适当调整至 `32.dp` 或 `14.sp`。
  - 注意：设置页 `SettingsScreen` 的顶栏同样为 `44.dp`，调整时应保持两处一致。
- **状态栏避让控制**：`.statusBarsPadding()` 确保了在任何全面屏、打孔屏、刘海屏设备上，顶部导航栏内容绝对不会与系统电池电量、时间图标重叠。

### 3.3 顶部核心交互功能实现
- **呼吸状态脉冲灯 (Breathing Pulse Indicator)**：
  ```kotlin
  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseAlpha by infiniteTransition.animateFloat(
      initialValue = 0.35f,
      targetValue = 1.0f,
      animationSpec = infiniteRepeatable(
          animation = tween(1400, easing = LinearEasing),
          repeatMode = RepeatMode.Reverse
      ),
      label = "pulse_alpha"
  )
  ```
- **主题调色板徽标**：右侧药丸显示当前主题家族名（如 "Editorial"）与主色圆点，**点击直接跳转设置页**（`onOpenSettings()`）。
  > 说明：文件中保留了 `DropdownMenu` / `DropdownMenuItem` 的 import，但当前版本**并未实现下拉菜单**（无 `expanded` 状态），属于未使用的遗留导入。

---

## 4. 推拽式侧边栏 (Push Canvas Sidebar) 动画开发架构

传统 Android 侧边栏多为浮动蒙层盖在主页上方（Modal Drawer）。本项目采用 **推拽式主画布结构（Sliding & Push Canvas Layout）**：**侧边栏展开时，右侧主页面被同步向右推开**。

### 4.1 双层平移与形变动画架构 (`MainActivity.kt`)
```kotlin
val sidebarWidth = 295.dp
// 豪华减速贝塞尔曲线，无突兀弹跳
val luxuryPushEasing = remember { CubicBezierEasing(0.16f, 1f, 0.3f, 1f) }

// 主画布向右推移距离：0dp -> 295dp
val pushOffset by animateDpAsState(
    targetValue = if (isSidebarOpen) sidebarWidth else 0.dp,
    animationSpec = tween(durationMillis = 320, easing = luxuryPushEasing),
    label = "push_offset"
)

// 侧边栏滑入位移：-295dp -> 0dp
val sidebarOffset by animateDpAsState(
    targetValue = if (isSidebarOpen) 0.dp else (-sidebarWidth),
    animationSpec = tween(durationMillis = 320, easing = luxuryPushEasing),
    label = "sidebar_offset"
)

// 主画布被推开时的圆角收缩与投影提升
val mainCornerRadius by animateDpAsState(
    targetValue = if (isSidebarOpen) 18.dp else 0.dp,
    animationSpec = tween(durationMillis = 320, easing = luxuryPushEasing)
)
val mainElevation by animateDpAsState(
    targetValue = if (isSidebarOpen) 14.dp else 0.dp,
    animationSpec = tween(durationMillis = 320, easing = luxuryPushEasing)
)
```

### 4.2 容器层级布局
```kotlin
Box(modifier = modifier.fillMaxSize().background(currentTheme.card)) {
    // 1. 底层左侧：固定锚定的侧边栏 (位移槽宽 295dp, 伴随 sidebarOffset 平移)
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(sidebarWidth)
            .offset(x = sidebarOffset)
    ) {
        AppSidebarContent(...)   // 内容自身声明 width(300.dp)
    }

    // 2. 表层右侧：主屏幕 (伴随 pushOffset 向右整体位移，并附带圆角与阴影)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .offset(x = pushOffset)
            .shadow(mainElevation, shape = RoundedCornerShape(mainCornerRadius), clip = false)
            .clip(RoundedCornerShape(mainCornerRadius))
            .border(
                width = if (isSidebarOpen) 1.dp else 0.dp,
                color = if (isSidebarOpen) currentTheme.border else Color.Transparent,
                shape = RoundedCornerShape(mainCornerRadius)
            )
    ) {
        Scaffold(...) { /* 主内容渲染区：4 标签路由 */ }
    }

    // 3. 点击回弹拦截层：主页被推开后，点击露出的主页任意区域即可直接平滑拉回
    if (isSidebarOpen) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.04f))
                .clickable { isSidebarOpen = false }
                .testTag("push_canvas_overlay_dismiss")
        )
    }
}
```

### 4.3 侧边栏内容结构 (`AppSidebarContent`)
抽屉内容自上而下分为 5 个区块：
1. **工作区头部**：34dp 衬线斜体 "H" 头像 + "Hello Studio" 名称（附 "PRO" 徽标）+ 副标题 "Design Systems Lab" + 关闭按钮。
2. **NAVIGATION**：4 个导航项（Craft Canvas / Typography Studio / CSS Design Tokens / Settings & Themes），选中态带主色圆点。
3. **PALETTES**：Light/Dark 一键互切开关 + 6 大家族预设列表（带主色圆点与选中 ✓）。
4. **QUICK ACTIONS**：Inspect CSS Tokens（打开审查器）、Export :root CSS（复制 CSS 到剪贴板）、Replay Splash Screen（重播启动页）。
5. **页脚信息卡**：ENGINE = Jetpack Compose，STANDARD = W3C CSS Level 4。

### 4.4 物理返回手势集成
通过 `BackHandler(enabled = isSidebarOpen) { isSidebarOpen = false }`，用户在侧边栏打开时滑动返回或按返回键，优先平滑拉回主画布。

---

## 5. 底部导航栏 (BottomNavBar) 开发与触控适配

底部导航栏位于 `ui/components/BottomNavBar.kt`，通过 `Scaffold` 的 `bottomBar` 插槽挂载。

### 5.1 标签定义 (`NavigationTab` 枚举)
```kotlin
enum class NavigationTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    CANVAS("Canvas", Icons.Filled.Home, Icons.Outlined.Home, "bottom_tab_canvas"),
    TYPOGRAPHY("Type", Icons.Filled.FormatSize, Icons.Outlined.FormatSize, "bottom_tab_typography"),
    TOKENS("Tokens", Icons.Filled.Code, Icons.Outlined.Code, "bottom_tab_tokens"),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings, "bottom_tab_settings")
}
```

### 5.2 结构规范与特性
- **高度配置**：内容行 `52.dp`（内部每个 Tab 高 `46.dp`），外层通过 `.navigationBarsPadding()` 自动垫高适配各类手势导航栏与虚拟三键导航，顶部附 1px 分割线。
- **微胶囊激活指示器 (Micro-Pill Active Indicator)**：
  选中的 Tab 呈现带有 `currentTheme.subtleSurface` 背景的高光微胶囊（`radiusSm` 圆角）。
- **颜色过渡动画**（图标色 / 文字色 / 胶囊底色三路并行）：
  ```kotlin
  val iconColor by animateColorAsState(
      targetValue = if (isSelected) currentTheme.primary else currentTheme.mutedForeground,
      animationSpec = tween(200),
      label = "tab_icon_color"
  )
  ```
- **图标与文字规格**：选中/未选中图标自动切换（Filled/Outlined），图标 `19.dp`，标签文字 `10.sp`（选中加粗）。
- **无障碍与触控尺寸**：每个 Item 通过 `weight(1f)` 均分宽度且高度 46dp，满足最小可交互触控规范，配有语义化 `contentDescription` 与 `testTag`。

---

## 6. 启动页打字机动态效果 (SplashScreen Engine) 开发

启动页位于 `ui/screens/SplashScreen.kt`，包含**动态光环、呼吸徽标、打字机文本引擎、多阶段进度条与跳过/重播逻辑**。

### 6.1 打字机协程状态机开发
采用 `LaunchedEffect(Unit)` 结合微延迟队列驱动逐字打印：

```kotlin
val phrases = remember {
    listOf(
        "Initializing Minimal Studio Engine...",
        "Loading CSS Variable Tokens...",
        "Synthesizing Typographic Scale...",
        "Hello, World." // 最终揭晓
    )
}

LaunchedEffect(Unit) {
    delay(350)
    for (i in phrases.indices) {
        phraseIndex = i
        val currentPhrase = phrases[i]
        displayedText = ""
        isTypingComplete = false

        // 拟人化打字节奏：普通日志 28ms/字，最终标语 42ms/字
        for (charIndex in 1..currentPhrase.length) {
            displayedText = currentPhrase.substring(0, charIndex)
            val typingDelay = if (i == phrases.lastIndex) 42L else 28L
            delay(typingDelay)
        }
        isTypingComplete = true

        if (i < phrases.lastIndex) {
            delay(400) // 阶段间歇停顿
        } else {
            isAllCompleted = true
            delay(850)
            onFinish() // 全部完成自动平滑进入主页
        }
    }
}
```

### 6.2 呼吸闪烁光标与 360° 自转光环
- **闪烁控制台光标 `▌`**：
  ```kotlin
  val infiniteTransition = rememberInfiniteTransition(label = "splash_cursor")
  val cursorAlpha by infiniteTransition.animateFloat(
      initialValue = 1f,
      targetValue = 0f,
      animationSpec = infiniteRepeatable(
          animation = tween(durationMillis = 480, easing = LinearEasing),
          repeatMode = RepeatMode.Reverse
      ),
      label = "cursor_blink"
  )
  ```
- **外圈 360° 自转扫描渐变**（92dp 圆环，`Brush.sweepGradient` 1.5dp 描边）：
  ```kotlin
  val rotationAngle by infiniteTransition.animateFloat(
      initialValue = 0f,
      targetValue = 360f,
      animationSpec = infiniteRepeatable(
          animation = tween(durationMillis = 12000, easing = LinearEasing),
          repeatMode = RepeatMode.Restart
      ),
      label = "ring_rotation"
  )
  ```
- **中心徽标呼吸**：68dp 圆角方块（"HW" 衬线斜体 24sp）以 `0.96f ~ 1.04f` 缩放脉动（1800ms，FastOutSlowInEasing）。
- **阶段进度条**：3dp 圆头进度条按 `(phraseIndex + 1) / 4` 推进（tween 350ms），配合 "PHASE 0X / 04" 与 EXEC/READY 状态灯。
- **环境光晕**：`drawBehind` 绘制主色 8% 透明度的径向渐变背景光。

### 6.3 交互与重播入口
1. **跳过按钮 (Skip Button)**：右上角提供随时点击即刻进入主页的通道（`testTag = "splash_skip_btn"`），全部完成后文案变为 "Enter"。
2. **完成进入按钮**：全部打字完成后底部浮现 "Launch Studio Canvas" 主色按钮（`testTag = "splash_enter_btn"`）。
3. **多入口重播支持 (Replay Splash)**：
   - 侧边栏 **"QUICK ACTIONS" -> "Replay Splash Screen"**（`sidebar_replay_splash_btn`）
   - 设置页面系统信息区 **"Preview Boot Splash Animation"**（`settings_replay_splash_btn`）
   - 触发时置位 `showSplashScreen = true`，自动通过 `AnimatedContent`（fadeIn 350ms / fadeOut 250ms）重新播放完整动画序列。

---

## 7. 字体排印系统与三大字体族 (Typography Engine)

应用内置三大经典排印方案，统一定义在 `SettingsScreen.kt` 的 `AppTypographyChoice` 中：

```kotlin
enum class AppTypographyChoice(val title: String, val subtitle: String, val font: FontFamily) {
    EDITORIAL("Editorial Serif", "Playfair / Georgia style", FontFamily.Serif),
    SANS("Modern Sans", "Inter / SF Pro style", FontFamily.SansSerif),
    MONO("Technical Mono", "Geist Mono / Fira style", FontFamily.Monospace)
}
```

- **Editorial Serif**：衬线体，赋予经典报刊文学与高端奢品特质。主画布大标语中第一部分使用衬线斜体（`FontStyle.Italic`）。
- **Modern Sans**：中性无衬线，提供极高清晰度与现代科技感。
- **Technical Mono**：等宽代码体，带来严谨工程与终端美感。

主画布内还有一份本地映射 `TypographyStyle`（`MainActivity.kt` 私有枚举，标签为 "Serif / Sans / Mono"），用于画布内的三段式字体切换器，与 `AppTypographyChoice` 一一映射。

> 补充：`theme/Type.kt` 定义了 Material 3 `Typography` 全套文字样式（displayLarge ~ labelSmall），供 `MaterialTheme` 使用；但各屏幕的实际文字均直接指定 `fontSize/fontFamily/fontWeight`，较少引用该排版系统。

---

## 8. CSS 变量实时审查器与各功能屏幕实装

### 8.1 CSS 变量实时审查器 (`CssVariableInspector.kt`)
- **架构**：采用 Material 3 `ModalBottomSheet`（`skipPartiallyExpanded = true`），从主画布、顶栏药丸、侧边栏或设置页均可展开，`testTag = "css_variable_inspector_sheet"`。
- **双标签结构**：
  - **CSS Code 页签**：以等宽字体展示 `currentTheme.toCssString()` 生成的代码块（深色主题代码色 `#38BDF8`，浅色 `#0284C7`），下方 44dp 主色按钮一键复制。
  - **Tokens 页签**：9 个色彩令牌色板（`--bg / --fg / --card / --border / --primary / --muted / --muted-fg / --accent / --ring`，24dp 圆形色样 + Hex 值）+ **Live Override --primary** 实时主色覆盖区（9 个预设色样：`#0070F3 / #5E6AD2 / #FF5500 / #10B981 / #EB5757 / #F59E0B / #8B5CF6 / #FAFAFA / #18181B`）。
- **主色覆盖机制**：选中自定义色后回调 `onCustomPrimarySelected`，MainActivity 中执行
  `onThemeChange(currentTheme.copy(primary = it, ring = it, accent = it))`，同时改写三个令牌。
- **实际导出的 CSS 内容**（`toCssString()`，与 Web 标准 `:root` 块对应）：
  ```
  :root, [data-theme="editorial-light"] {   /* 深色主题则为 .dark-theme 选择器 */
    --bg / --text / --fg / --surface / --card / --card-fg / --border /
    --accent / --primary / --primary-fg / --muted / --muted-bg / --ring / --radius
  }
  ```
  > 注意：当前仅导出单一 `--radius`（取 `radiusLg`），**不导出** `--radius-sm/md/lg` 分级圆角，也**不含**字体栈变量。

### 8.2 排印工作室 (`TypeStudioScreen.kt`)
- **字体族切换器**：3 张 "Ag" 预览卡片（`type_studio_choice_*`），选中态 1.5dp 主色描边；右上角附 **Italic 斜体快速开关**。
- **样张看板 (Specimen Hero Canvas)**：以当前字体/字号/字距实时渲染设计名言（内置 4 句，如 "Form follows function."），底部圆点切换器（选中态为 18×8dp 主色药丸），右上角可复制当前样张文本。
- **双滑块实时微调**：
  - 字号：`24f..64f`（默认 42f），实时显示 "XX sp"。
  - 字距 (Tracking)：`-2.0f..4.0f`（默认 -0.5f），实时显示一位小数。
  - 行高自动按 `fontSize × 1.15` 联动。
- **字形样张 (Glyph Specimen)**：A-Z 大写、a-z 小写、数字与符号三行字符集预览。
- 滑块颜色（拇指/激活轨/非激活轨）全部绑定当前主题令牌，随主题热切换。

### 8.3 设计令牌面板 (`TokensScreen.kt`)
- **实时搜索与过滤 (Token Search)**：`OutlinedTextField`（`tokens_search_input`）支持按令牌键名（如 `--primary`）或用途描述实时筛选。
- **令牌矩阵**：内置 9 个令牌条目（`--background / --foreground / --primary / --primary-foreground / --card / --muted / --muted-foreground / --border / --ring`），22dp 方形色样 + 等宽 Hex 值，**点击任意条目即复制对应 Hex 颜色**并 Toast 提示。
- **交互式组件演练场 (Live Tokenized UI Components)**：
  - `var(--primary)` 主色计数器按钮（点击递增）与 `var(--muted)` 弱化底色递减按钮；
  - "Badge Active" 高亮徽标（主色 12% 底 + 30% 描边）与圆角规格徽标（实时显示 `radiusLg` 像素值）。

### 8.4 系统设置页面 (`SettingsScreen.kt`)
设置页为独立 `Scaffold`（顶栏同为 44dp：返回按钮 + "Settings" 标题 + "Design System" 等宽徽标），内容分 5 个区块：
1. **COLOR SCHEME & MODE**：Light Canvas / Dark OLED 双卡片明暗选择器（选中态 2dp 主色描边 + 主色对勾圆点），切换时保持当前主题家族。
2. **CURATED DESIGN PALETTES**：**单列分组列表**呈现 6 大家族（名称 + 描述 + 双色点预览：目标变体的 primary 与 card），点击即刻热重载全局主题。
3. **TYPOGRAPHY ENGINE**：3 个字体族列表项（"Aa" 字形预览 + 标题/副标题），切换全局默认排版。
4. **DEVELOPER & DESIGN TOKENS**：Copy CSS Root Tokens（复制 `:root` 导出，带 1.8s "Copied!" 状态反馈）+ Live CSS Variable Inspector 入口行。
5. **APPLICATION & SYSTEM INFO**：版本号（1.0.0 Production）、设计标准（CSS Custom Tokens W3C）、当前主色 Hex、圆角规格四项信息，以及 **Preview Boot Splash Animation** 启动页重播按钮。

> 说明：顶栏右侧的 "Design System" 徽标为**静态展示元素**（源码注释残留 "Reset" 字样），当前版本**没有**一键重置功能。

### 8.5 主画布交互工作台 (`MainActivity.kt` Canvas)
CANVAS 标签页自上而下包含：
1. **遥测头部**："DESIGN TOKENS" 标签 + 延迟药丸（显示 `3~5ms` 的**模拟随机值**，非真实渲染耗时，随主题切换刷新）+ "Inspect CSS" 快捷药丸。
2. **调色板横向微切换器**：6 家族药丸（`preset_pill_*`），带目标变体主色圆点，点击即切换对应明暗变体。
3. **中央大标语卡片 (Hero Canvas)**：
   - **交错入场动画**：全部采用豪华曲线 `CubicBezierEasing(0.16f, 1f, 0.3f, 1f)`，卡片 650ms/延迟60ms → 眉标 500ms/120ms → 第一行 700ms/180ms → 第二行 750ms/300ms → 主色横线宽度展开 600ms/420ms → 说明文字 600ms/480ms。
   - **微物理按压回弹**：`pointerInput + detectTapGestures`，按下时 `0.985f` 缩放，配合 `spring(MediumBouncy, StiffnessLow)` 回弹；点按循环切换 6 组策展标语（"Hello / World."、"Less, / Better." 等）。
   - **双行大标语排版**：66sp 双段式（第一部分衬线斜体 Light + 第二部分无衬线 Bold，letterSpacing -2.8sp），随字体引擎切换。
   - **计数徽标**：等宽字体 "N / 6" 循环指示。
4. **自定义问候语编辑器**：`AnimatedVisibility` 展开的双输入框（衬线斜体前段 + 无衬线粗体后段），输入即实时投射到大标语；点击大标语可退出自定义模式。
5. **字体引擎三段选择器**：Serif / Sans / Mono 等宽分段控件。
6. **CSS Variables 卡片**：7 行令牌速览（`--bg / --text / --surface / --accent / --muted / --border / --radius`，带 12dp 色样）+ "Copy CSS" 复制按钮（1.8s "Copied" 状态反馈）。

---

## 9. 剪贴板交互、内存态状态与触控无障碍设计

### 9.1 剪贴板交互（内联实现）
所有复制功能均采用 Android 原生 `ClipboardManager` 系统服务。**当前版本没有统一的封装函数**，相同逻辑在多处内联重复（主画布、审查器、设置页、侧边栏、令牌面板、排印工作室共 6 处）：

```kotlin
val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
val clip = ClipData.newPlainText("CSS Variables", currentTheme.toCssString())
clipboard.setPrimaryClip(clip)
Toast.makeText(context, "CSS Variables Copied", Toast.LENGTH_SHORT).show()
```

多数复制按钮附带限时状态反馈：`isCopied = true` 后经 `scope.launch { delay(1800~2500); isCopied = false }` 自动还原图标与文案。

> 重构建议：可抽取统一的 `copyToClipboard(context, text, label)` 扩展函数消除重复。

### 9.2 状态管理：纯内存态（无持久化）
- 全局主题、当前标签、字体选择、自定义问候语等状态均为 `remember { mutableStateOf(...) }` **内存态**，由 `MainActivity` 单点持有并经参数下传（UDF）。
- **当前版本没有任何持久化机制**（未使用 DataStore / SharedPreferences / Room），进程重启后所有个性化选择恢复默认（主题回到 Editorial Light）。
- 状态也未经过 `rememberSaveable` 保存，配置变更（如旋转）场景依赖 Activity 默认行为。

### 9.3 触控无障碍与自动化测试 (`testTag`)
- **Touch Target**：底部标签 46dp 高、侧边栏列表项与设置行均有充足点击区域；部分顶栏小按钮为 28dp（低于 48dp 建议值，属紧凑设计的取舍）。
- **自动化测试标识 (Compose TestTags)**：核心交互元素均绑定 `snake_case` 标识符，例如 `top_nav_sidebar_btn`、`top_nav_settings_btn`、`top_nav_theme_summary_badge`、`bottom_tab_*`、`sidebar_item_*`、`splash_skip_btn`、`splash_enter_btn`、`settings_mode_light_card`、`settings_palette_item_*`、`tokens_search_input`、`type_studio_choice_*`、`css_variable_inspector_sheet`、`copy_css_btn` 等。
- 测试基建：Robolectric + Roborazzi 截图测试（`GreetingScreenshotTest`），但当前截图测试仍基于旧版 `HelloWorldApp` 双参数签名，需随新架构更新。

---

## 10. 核心组件参数速查与调优指南

| 组件 / 功能 | 核心源文件 | 核心参数 / 推荐数值 | 调参说明与业务影响 |
| :--- | :--- | :--- | :--- |
| **顶部导航栏高度** | `TopNavBar.kt` / `SettingsScreen.kt` | `height(44.dp)`（两处一致） | 调整 `Row` 高度可改变导航栏厚度；`.statusBarsPadding()` 负责系统状态栏避让。 |
| **推拽侧边栏宽度** | `MainActivity.kt` | `sidebarWidth = 295.dp` | 改变侧边栏宽度与主画布被推开的最大水平位移量（内容自身声明 300dp，注意同步）。 |
| **推拽减速曲线** | `MainActivity.kt` | `CubicBezierEasing(0.16f, 1f, 0.3f, 1f)` / 320ms | 控制主画布推拉时的加速度与阻尼感。 |
| **主页推开圆角与阴影** | `MainActivity.kt` | `18.dp` (圆角) / `14.dp` (阴影) | 调整侧边栏展开时主画布的立体质感。 |
| **底部导航栏高度** | `BottomNavBar.kt` | 内容行 `height(52.dp)` / 单项 `46.dp` | 调整 Tab 栏高度；`.navigationBarsPadding()` 负责底部手势区避让。 |
| **主题切换背景过渡** | `MainActivity.kt` | `tween(280, FastOutSlowInEasing)` | 全局背景色插值过渡时长与曲线。 |
| **大标语微按压缩放** | `MainActivity.kt` | `0.985f` + spring(MediumBouncy, StiffnessLow) | 按压反馈的收缩幅度与回弹手感。 |
| **入场交错动画** | `MainActivity.kt` | 650/500/700/750/600/600ms，延迟 60→480ms | 各元素入场时长与级联延迟（豪华曲线）。 |
| **打字机打字速率** | `SplashScreen.kt` | 普通日志 `28ms` / 终句 `42ms` | 减小数值打字变快，增大数值打字节奏变慢。 |
| **光标闪烁周期** | `SplashScreen.kt` | `480ms` (tween) | 控制终端光标 `▌` 的忽明忽暗闪烁节拍。 |
| **光环自转速度** | `SplashScreen.kt` | `12000ms` (12秒一整圈) | 调整外圈渐变环的旋转周期。 |
| **字号滑块范围** | `TypeStudioScreen.kt` | `24f..64f`（默认 42f） | 排印工作室样张字号调节范围。 |
| **字距滑块范围** | `TypeStudioScreen.kt` | `-2.0f..4.0f`（默认 -0.5f） | 排印工作室 Tracking 调节范围。 |
| **主题色彩令牌** | `CssTokens.kt` | `ProductionPalettes.*`（12 套） | 新增或微调任意主题色彩令牌十六进制值。 |
| **卡片与主圆角** | `CssTokens.kt` | `radiusSm=10 / radiusMd=16 / radiusLg=24.dp` | 调整圆角大小，全局卡片与面板自适应缩放。 |

---

## 11. 已知局限与工程问题

结合当前代码核验，以下问题客观存在，供后续迭代参考：

1. **无状态持久化**：主题、字体、自定义文案重启即丢失（见 §9.2）。
2. **剪贴板逻辑 6 处重复**：无统一封装（见 §9.1）。
3. **`themeId` 前缀识别逻辑 4 处重复**，且 `else -> "Braun"` 兜底脆弱。
4. **未使用的依赖**：Retrofit、OkHttp、Moshi、Room 等仍打入 APK，源码零使用（Firebase/Gemini 相关依赖与 AI Studio 模板残留已移除）。
5. **release 未启用混淆**：`isMinifyEnabled = false`。
6. **未使用的 import**：如 `TopNavBar.kt` 的 `DropdownMenu`/`DropdownMenuItem`、`MainActivity.kt` 的多个图标与绘制类导入。
7. **弃用 API**：`TypographyStyle.values()`、`NavigationTab.values()`、`AppTypographyChoice.values()` 在 Kotlin 2.x 应改用 `.entries`。
8. **虚假遥测**：主画布 "Xms" 延迟为 `3~5` 随机数，界面呈现易被误解为真实性能指标。
9. **硬编码文案**：所有 UI 字符串未走 `strings.xml`，无法国际化。
10. **截图测试滞后**：`GreetingScreenshotTest` 仍按旧版接口调用 `HelloWorldApp`，需适配新的多标签架构。

---
*本文档已按工作区实际代码全面核验修订，与 2026-08-28 时点的源码状态一致。*
