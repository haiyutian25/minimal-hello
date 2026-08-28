package com.example.feature.greeting.impl

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.feature.greeting.impl.components.AppSidebarContent
import com.example.feature.greeting.impl.components.CssVariableInspectorSheet
import com.example.feature.greeting.impl.components.NavigationTab
import com.example.feature.greeting.impl.components.ProductionBottomNavBar
import com.example.feature.greeting.impl.components.ProductionTopNavBar
import com.example.feature.greeting.impl.screens.CanvasScreen
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

    val animatedBg by animateColorAsState(
        targetValue = currentTheme.background,
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "bg_color"
    )

    val sidebarWidth = 295.dp
    val luxuryPushEasing = remember { CubicBezierEasing(0.16f, 1f, 0.3f, 1f) }

    val pushOffset by animateDpAsState(
        targetValue = if (isSidebarOpen) sidebarWidth else 0.dp,
        animationSpec = tween(durationMillis = 320, easing = luxuryPushEasing),
        label = "push_offset"
    )
    val sidebarOffset by animateDpAsState(
        targetValue = if (isSidebarOpen) 0.dp else (-sidebarWidth),
        animationSpec = tween(durationMillis = 320, easing = luxuryPushEasing),
        label = "sidebar_offset"
    )
    val mainCornerRadius by animateDpAsState(
        targetValue = if (isSidebarOpen) 18.dp else 0.dp,
        animationSpec = tween(durationMillis = 320, easing = luxuryPushEasing),
        label = "main_corner_radius"
    )
    val mainElevation by animateDpAsState(
        targetValue = if (isSidebarOpen) 14.dp else 0.dp,
        animationSpec = tween(durationMillis = 320, easing = luxuryPushEasing),
        label = "main_elevation"
    )

    BackHandler(enabled = isSidebarOpen) {
        viewModel.closeSidebar()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(currentTheme.card)
    ) {
        // 1. Main Screen (Directly pushed to the right when sidebar expands)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = pushOffset)
                .shadow(
                    elevation = mainElevation,
                    shape = RoundedCornerShape(mainCornerRadius),
                    clip = false
                )
                .clip(RoundedCornerShape(mainCornerRadius))
                .border(
                    width = if (isSidebarOpen) 1.dp else 0.dp,
                    color = if (isSidebarOpen) currentTheme.border else Color.Transparent,
                    shape = RoundedCornerShape(mainCornerRadius)
                )
        ) {
            Scaffold(
                containerColor = animatedBg,
                contentColor = currentTheme.foreground,
                topBar = {
                    ProductionTopNavBar(
                        currentTheme = currentTheme,
                        onOpenSidebar = viewModel::toggleSidebar
                    )
                },
                bottomBar = {
                    ProductionBottomNavBar(
                        currentTab = currentTab,
                        onTabSelected = { viewModel.selectTab(it) },
                        currentTheme = currentTheme
                    )
                },
                modifier = Modifier.fillMaxSize()
            ) { innerPadding ->
                when (currentTab) {
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
                    NavigationTab.SETTINGS -> SettingsScreen(
                        currentTheme = currentTheme,
                        onThemeChange = viewModel::selectTheme,
                        selectedTypography = typographyChoice,
                        onTypographyChange = viewModel::selectTypography,
                        onOpenInspector = viewModel::showInspector,
                        onReplaySplash = onReplaySplash,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }

        // 2. Tap-outside interceptor: geometrically covers ONLY the area right of the
        // sidebar, so taps on the sidebar itself can never fall through to it
        // (unconsumed taps on non-interactive sidebar areas would otherwise
        // propagate down to a full-screen interceptor and collapse the drawer).
        if (isSidebarOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = sidebarWidth)
                    .clickable { viewModel.closeSidebar() }
                    .testTag("sidebar_outside_dismiss")
            )
        }

        // 3. Sliding Sidebar (Anchored on the left, drawn above the interceptor)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(sidebarWidth)
                .offset(x = sidebarOffset)
        ) {
            AppSidebarContent(
                currentTheme = currentTheme,
                onOpenSettings = { viewModel.selectTab(NavigationTab.SETTINGS) },
                onCloseDrawer = viewModel::closeSidebar,
                modifier = Modifier.fillMaxSize()
            )
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
