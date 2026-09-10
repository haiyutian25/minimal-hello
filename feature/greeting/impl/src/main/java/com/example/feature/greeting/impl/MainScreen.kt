package com.example.feature.greeting.impl

import android.widget.Toast
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.ui.base.util.EventsEffect
import com.example.feature.greeting.impl.components.CssVariableInspectorSheet
import com.example.feature.greeting.impl.components.NavigationTab
import com.example.feature.greeting.impl.components.ProductionBottomNavBar
import com.example.feature.greeting.impl.components.ProductionTopNavBar
import com.example.feature.greeting.impl.components.SidebarDrawer
import com.example.feature.greeting.impl.components.SidebarEdgeZone
import com.example.feature.greeting.impl.screens.CanvasScreen
import com.example.feature.greeting.impl.screens.FontScreen
import com.example.feature.greeting.impl.screens.FontSizeScreen
import com.example.feature.greeting.impl.screens.LanguageScreen
import com.example.feature.greeting.impl.screens.SettingsMenuScreen
import com.example.feature.greeting.impl.screens.SettingsScreen
import com.example.feature.greeting.impl.screens.TokensScreen
import com.example.feature.greeting.impl.screens.TypeStudioScreen

/**
 * Post-splash experience: push-canvas sidebar + 4-tab scaffold + global
 * CSS inspector sheet. Renders the single [GreetingState] exposed by
 * [GreetingViewModel.stateFlow] and sends every user intent back as a
 * [GreetingAction] (unidirectional data flow).
 */
@Composable
fun MainScreen(
    viewModel: GreetingViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    // Consume one-time UI events (toasts) exactly once, lifecycle-aware.
    val eventContext = LocalContext.current
    EventsEffect(viewModel = viewModel) { event ->
        when (event) {
            is GreetingEvent.ShowToast ->
                Toast.makeText(eventContext, event.messageRes, Toast.LENGTH_SHORT).show()
        }
    }

    val animatedBg by animateColorAsState(
        targetValue = state.theme.background,
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "bg_color"
    )

    // Settings levels step back first; the sidebar (registered later, thus
    // dispatched first when both are active) still takes priority when open.
    BackHandler(enabled = state.settingsLevel != SettingsLevel.NONE) {
        viewModel.trySendAction(GreetingAction.SettingsBackPressed)
    }
    BackHandler(enabled = state.isSidebarOpen) {
        viewModel.trySendAction(GreetingAction.SidebarClosed)
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Push-canvas sidebar drawer; the main scaffold is its pushed content.
        SidebarDrawer(
            isOpen = state.isSidebarOpen,
            currentTheme = state.theme,
            onOpen = { viewModel.trySendAction(GreetingAction.SidebarOpened) },
            onOpenSettings = { viewModel.trySendAction(GreetingAction.SettingsMenuOpened) },
            onClose = { viewModel.trySendAction(GreetingAction.SidebarClosed) }
        ) {
            Scaffold(
                containerColor = animatedBg,
                contentColor = state.theme.foreground,
                topBar = {
                    ProductionTopNavBar(
                        currentTheme = state.theme,
                        onOpenSidebar = { viewModel.trySendAction(GreetingAction.SidebarToggled) },
                        pageTitle = when (state.settingsLevel) {
                            SettingsLevel.MENU -> stringResource(R.string.settings_page_title)
                            SettingsLevel.PAGE -> stringResource(R.string.settings_menu_appearance_title)
                            SettingsLevel.FONT -> stringResource(R.string.settings_menu_font_title)
                            SettingsLevel.FONT_SIZE -> stringResource(R.string.settings_font_size_label)
                            SettingsLevel.LANGUAGE -> stringResource(R.string.language_title)
                            SettingsLevel.NONE -> null
                        },
                        onBack = { viewModel.trySendAction(GreetingAction.SettingsBackPressed) }
                    )
                },
                modifier = Modifier.fillMaxSize()
            ) { innerPadding ->
                // Settings flow renders inside the Scaffold content area so the
                // global top nav bar (Scaffold topBar) persists on every level.
                when (state.settingsLevel) {
                    SettingsLevel.MENU -> SettingsMenuScreen(
                        currentTheme = state.theme,
                        onOpenAppearance = {
                            viewModel.trySendAction(GreetingAction.AppearanceSettingsOpened)
                        },
                        onOpenFont = {
                            viewModel.trySendAction(GreetingAction.FontSettingsOpened)
                        },
                        onOpenLanguage = {
                            viewModel.trySendAction(GreetingAction.LanguageSettingsOpened)
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                    SettingsLevel.LANGUAGE -> LanguageScreen(
                        currentTheme = state.theme,
                        modifier = Modifier.padding(innerPadding)
                    )
                    SettingsLevel.PAGE -> SettingsScreen(
                        currentTheme = state.theme,
                        onThemeChange = {
                            viewModel.trySendAction(GreetingAction.ThemeSelected(it))
                        },
                        colorMode = state.colorMode,
                        onColorModeChange = {
                            viewModel.trySendAction(GreetingAction.ColorModeChanged(it))
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                    SettingsLevel.FONT -> FontScreen(
                        currentTheme = state.theme,
                        selectedTypography = state.typographyChoice,
                        onTypographyChange = {
                            viewModel.trySendAction(GreetingAction.TypographySelected(it))
                        },
                        fontScale = state.fontScale,
                        onOpenFontSize = {
                            viewModel.trySendAction(GreetingAction.FontSizeSettingsOpened)
                        },
                        installedFonts = state.installedFonts,
                        activeCustomFontId = state.activeCustomFontId,
                        downloadProgress = state.downloadProgress,
                        fontFamilyFor = viewModel::customFontFamily,
                        onSelectCustomFont = {
                            viewModel.trySendAction(GreetingAction.CustomFontSelected(it))
                        },
                        onDeleteCustomFont = {
                            viewModel.trySendAction(GreetingAction.FontDeleteClicked(it))
                        },
                        onDownloadFont = {
                            viewModel.trySendAction(GreetingAction.FontDownloadClicked(it))
                        },
                        onImportFont = { uri, name ->
                            viewModel.trySendAction(GreetingAction.FontImportRequested(uri, name))
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                    SettingsLevel.FONT_SIZE -> FontSizeScreen(
                        currentTheme = state.theme,
                        fontScale = state.fontScale,
                        onSave = { viewModel.trySendAction(GreetingAction.FontScaleSaved(it)) },
                        modifier = Modifier.padding(innerPadding)
                    )
                    // Bottom navigation hosts the pages itself; swipe-to-switch
                    // is its optional feature. While the drawer is open, drags
                    // keep closing it, and the left edge zone stays reserved
                    // for the drawer's edge swipe.
                    SettingsLevel.NONE -> ProductionBottomNavBar(
                        currentTab = state.currentTab,
                        onTabSelected = {
                            viewModel.trySendAction(GreetingAction.TabSelected(it))
                            viewModel.trySendAction(GreetingAction.SettingsExited)
                        },
                        currentTheme = state.theme,
                        swipeable = true,
                        swipeEnabled = !state.isSidebarOpen,
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
                                currentTheme = state.theme,
                                selectedTypography = state.typographyChoice,
                                onTypographyChange = {
                                    viewModel.trySendAction(GreetingAction.TypographySelected(it))
                                }
                            )
                            NavigationTab.TOKENS -> TokensScreen(
                                currentTheme = state.theme,
                                onOpenInspector = {
                                    viewModel.trySendAction(GreetingAction.InspectorShown)
                                }
                            )
                            // 4th tab is intentionally blank (settings moved to the sidebar flow)
                            NavigationTab.SETTINGS -> Box(modifier = Modifier.fillMaxSize())
                        }
                    }
                }
            }
        }

        // CSS Variables Inspector Bottom Sheet (Accessible from everywhere)
        if (state.isInspectorVisible) {
            CssVariableInspectorSheet(
                currentTheme = state.theme,
                onDismiss = { viewModel.trySendAction(GreetingAction.InspectorDismissed) },
                onCustomPrimarySelected = {
                    viewModel.trySendAction(GreetingAction.PrimaryColorOverridden(it))
                }
            )
        }
    }
}
