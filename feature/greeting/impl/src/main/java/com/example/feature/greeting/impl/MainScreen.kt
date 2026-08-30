package com.example.feature.greeting.impl

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.feature.greeting.impl.components.CssVariableInspectorSheet
import com.example.feature.greeting.impl.components.NavigationTab
import com.example.feature.greeting.impl.components.ProductionBottomNavBar
import com.example.feature.greeting.impl.components.ProductionTopNavBar
import com.example.feature.greeting.impl.components.SidebarDrawer
import com.example.feature.greeting.impl.components.SidebarEdgeZone
import com.example.feature.greeting.impl.screens.CanvasScreen
import com.example.feature.greeting.impl.screens.FontScreen
import com.example.feature.greeting.impl.screens.LanguageScreen
import com.example.feature.greeting.impl.screens.SettingsMenuScreen
import com.example.feature.greeting.impl.screens.SettingsScreen
import com.example.feature.greeting.impl.screens.TokensScreen
import com.example.feature.greeting.impl.screens.TypeStudioScreen

/**
 * Post-splash experience: push-canvas sidebar + 4-tab scaffold + global
 * CSS inspector sheet. All state is observed from [GreetingViewModel] (MVVM).
 */
@Composable
fun MainScreen(
    viewModel: GreetingViewModel,
    modifier: Modifier = Modifier,
) {
    val currentTheme by viewModel.currentTheme.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val isSidebarOpen by viewModel.isSidebarOpen.collectAsState()
    val isInspectorVisible by viewModel.isInspectorVisible.collectAsState()
    val typographyChoice by viewModel.typographyChoice.collectAsState()
    val fontScale by viewModel.fontScale.collectAsState()
    val colorMode by viewModel.colorMode.collectAsState()
    val settingsLevel by viewModel.settingsLevel.collectAsState()

    val animatedBg by animateColorAsState(
        targetValue = currentTheme.background,
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "bg_color"
    )

    // Settings levels step back first; the sidebar (registered later, thus
    // dispatched first when both are active) still takes priority when open.
    BackHandler(enabled = settingsLevel != SettingsLevel.NONE) {
        viewModel.backSettings()
    }
    BackHandler(enabled = isSidebarOpen) {
        viewModel.closeSidebar()
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Push-canvas sidebar drawer; the main scaffold is its pushed content.
        SidebarDrawer(
            isOpen = isSidebarOpen,
            currentTheme = currentTheme,
            onOpen = viewModel::openSidebar,
            onOpenSettings = { viewModel.openSettingsMenu() },
            onClose = viewModel::closeSidebar
        ) {
            Scaffold(
                containerColor = animatedBg,
                contentColor = currentTheme.foreground,
                topBar = {
                    ProductionTopNavBar(
                        currentTheme = currentTheme,
                        onOpenSidebar = viewModel::toggleSidebar,
                        pageTitle = when (settingsLevel) {
                            SettingsLevel.MENU -> stringResource(R.string.settings_page_title)
                            SettingsLevel.PAGE -> stringResource(R.string.settings_menu_appearance_title)
                            SettingsLevel.FONT -> stringResource(R.string.settings_menu_font_title)
                            SettingsLevel.LANGUAGE -> stringResource(R.string.language_title)
                            SettingsLevel.NONE -> null
                        },
                        onBack = { viewModel.backSettings() }
                    )
                },
                modifier = Modifier.fillMaxSize()
            ) { innerPadding ->
                // Settings flow renders inside the Scaffold content area so the
                // global top nav bar (Scaffold topBar) persists on every level.
                when (settingsLevel) {
                    SettingsLevel.MENU -> SettingsMenuScreen(
                        currentTheme = currentTheme,
                        onOpenAppearance = viewModel::openAppearanceSettings,
                        onOpenFont = viewModel::openFontSettings,
                        onOpenLanguage = viewModel::openLanguageSettings,
                        modifier = Modifier.padding(innerPadding)
                    )
                    SettingsLevel.LANGUAGE -> LanguageScreen(
                        currentTheme = currentTheme,
                        modifier = Modifier.padding(innerPadding)
                    )
                    SettingsLevel.PAGE -> SettingsScreen(
                        currentTheme = currentTheme,
                        onThemeChange = viewModel::selectTheme,
                        colorMode = colorMode,
                        onColorModeChange = viewModel::setColorMode,
                        modifier = Modifier.padding(innerPadding)
                    )
                    SettingsLevel.FONT -> FontScreen(
                        currentTheme = currentTheme,
                        selectedTypography = typographyChoice,
                        onTypographyChange = viewModel::selectTypography,
                        fontScale = fontScale,
                        onFontScaleChange = viewModel::setFontScale,
                        modifier = Modifier.padding(innerPadding)
                    )
                    // Bottom navigation hosts the pages itself; swipe-to-switch
                    // is its optional feature. While the drawer is open, drags
                    // keep closing it, and the left edge zone stays reserved
                    // for the drawer's edge swipe.
                    SettingsLevel.NONE -> ProductionBottomNavBar(
                        currentTab = currentTab,
                        onTabSelected = {
                            viewModel.selectTab(it)
                            viewModel.exitSettings()
                        },
                        currentTheme = currentTheme,
                        swipeable = true,
                        swipeEnabled = !isSidebarOpen,
                        excludedStartZone = SidebarEdgeZone,
                        // Flush with the screen bottom: the bar must stay exactly
                        // its 66dp content height, so drop the Scaffold's
                        // navigation-bar inset from its bottom padding.
                        modifier = Modifier.padding(
                            PaddingValues(
                                start = innerPadding.calculateLeftPadding(LocalLayoutDirection.current),
                                top = innerPadding.calculateTopPadding(),
                                end = innerPadding.calculateRightPadding(LocalLayoutDirection.current),
                                bottom = 0.dp
                            )
                        )
                    ) { tab ->
                        when (tab) {
                            NavigationTab.CANVAS -> CanvasScreen(viewModel = viewModel)
                            NavigationTab.TYPOGRAPHY -> TypeStudioScreen(
                                currentTheme = currentTheme,
                                selectedTypography = typographyChoice,
                                onTypographyChange = viewModel::selectTypography
                            )
                            NavigationTab.TOKENS -> TokensScreen(
                                currentTheme = currentTheme,
                                onOpenInspector = viewModel::showInspector
                            )
                            // 4th tab is intentionally blank (settings moved to the sidebar flow)
                            NavigationTab.SETTINGS -> Box(modifier = Modifier.fillMaxSize())
                        }
                    }
                }
            }
        }

        // CSS Variables Inspector Bottom Sheet (Accessible from everywhere)
        if (isInspectorVisible) {
            CssVariableInspectorSheet(
                currentTheme = currentTheme,
                onDismiss = viewModel::hideInspector,
                onCustomPrimarySelected = viewModel::overridePrimary
            )
        }
    }
}
