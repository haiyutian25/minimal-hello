package com.example.feature.greeting.impl

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.feature.greeting.impl.components.CssVariableInspectorSheet
import com.example.feature.greeting.impl.components.NavigationTab
import com.example.feature.greeting.impl.components.ProductionBottomNavBar
import com.example.feature.greeting.impl.components.ProductionTopNavBar
import com.example.feature.greeting.impl.components.SidebarDrawer
import com.example.feature.greeting.impl.screens.CanvasScreen
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
    onReplaySplash: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentTheme by viewModel.currentTheme.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val isSidebarOpen by viewModel.isSidebarOpen.collectAsState()
    val isInspectorVisible by viewModel.isInspectorVisible.collectAsState()
    val typographyChoice by viewModel.typographyChoice.collectAsState()
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
                            SettingsLevel.MENU -> "Settings"
                            SettingsLevel.PAGE -> "Appearance & Themes"
                            SettingsLevel.NONE -> null
                        },
                        onBack = { viewModel.backSettings() }
                    )
                },
                bottomBar = {
                    // Bottom navigation belongs to the main tabs only;
                    // settings menu / settings pages render without it.
                    if (settingsLevel == SettingsLevel.NONE) {
                        ProductionBottomNavBar(
                            currentTab = currentTab,
                            onTabSelected = {
                                viewModel.selectTab(it)
                                viewModel.exitSettings()
                            },
                            currentTheme = currentTheme
                        )
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) { innerPadding ->
                // Settings flow renders inside the Scaffold content area so the
                // global top nav bar (Scaffold topBar) persists on every level.
                when (settingsLevel) {
                    SettingsLevel.MENU -> SettingsMenuScreen(
                        currentTheme = currentTheme,
                        onOpenAppearance = viewModel::openAppearanceSettings,
                        modifier = Modifier.padding(innerPadding)
                    )
                    SettingsLevel.PAGE -> SettingsScreen(
                        currentTheme = currentTheme,
                        onThemeChange = viewModel::selectTheme,
                        selectedTypography = typographyChoice,
                        onTypographyChange = viewModel::selectTypography,
                        onOpenInspector = viewModel::showInspector,
                        onReplaySplash = onReplaySplash,
                        modifier = Modifier.padding(innerPadding)
                    )
                    SettingsLevel.NONE -> when (currentTab) {
                        NavigationTab.CANVAS -> CanvasScreen(
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                        NavigationTab.TYPOGRAPHY -> TypeStudioScreen(
                            currentTheme = currentTheme,
                            selectedTypography = typographyChoice,
                            onTypographyChange = viewModel::selectTypography,
                            modifier = Modifier.padding(innerPadding)
                        )
                        NavigationTab.TOKENS -> TokensScreen(
                            currentTheme = currentTheme,
                            onOpenInspector = viewModel::showInspector,
                            modifier = Modifier.padding(innerPadding)
                        )
                        // 4th tab is intentionally blank (settings moved to the sidebar flow)
                        NavigationTab.SETTINGS -> Box(modifier = Modifier.fillMaxSize())
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
