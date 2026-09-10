package com.example.feature.greeting.impl

import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.lifecycle.viewModelScope
import com.example.core.data.model.ColorMode
import com.example.core.data.model.UserPreferences
import com.example.core.data.repository.GreetingRepository
import com.example.core.data.repository.HeroQuote
import com.example.core.data.repository.UserPreferencesRepository
import com.example.core.ui.base.BaseViewModel
import com.example.core.ui.theme.CssVariables
import com.example.core.ui.theme.ThemeResolver
import com.example.feature.greeting.impl.components.NavigationTab
import com.example.feature.greeting.impl.fonts.CustomFontRepository
import com.example.feature.greeting.impl.fonts.InstalledFont
import com.example.feature.greeting.impl.fonts.PresetFont
import com.example.feature.greeting.impl.screens.AppTypographyChoice
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * User-defined greeting overlay state (MVVM lifted from the canvas UI).
 */
data class CustomGreetingState(
    val part1: String = "Hello",
    val part2: String = "World.",
    val isActive: Boolean = false,
)

/**
 * Content-level settings navigation inside the main Scaffold content area
 * (the global top nav bar stays visible on every level):
 * NONE (normal tabs) -> MENU (settings menu list) -> PAGE (appearance settings).
 */
enum class SettingsLevel { NONE, MENU, PAGE, LANGUAGE, FONT, FONT_SIZE }

/**
 * Single immutable UI state for the greeting feature (UDF).
 *
 * [theme] and [activeContentFont] are derived from the raw preference fields by the
 * ViewModel on every state update; all other fields are set directly by actions.
 */
data class GreetingState(
    // Raw preference inputs
    val themeId: String,
    val colorMode: ColorMode,
    val primaryOverride: Color?,
    val isSystemDark: Boolean,
    // Derived
    val theme: CssVariables,
    val activeContentFont: FontFamily,
    // Navigation / chrome
    val currentTab: NavigationTab,
    val isSidebarOpen: Boolean,
    val isInspectorVisible: Boolean,
    val settingsLevel: SettingsLevel,
    // Typography
    val typographyChoice: AppTypographyChoice,
    val fontScale: Float,
    val activeCustomFontId: String,
    val installedFonts: List<InstalledFont>,
    val downloadProgress: Map<String, Float>,
    // Content
    val greetingIndex: Int,
    val customGreeting: CustomGreetingState,
    val heroQuotes: List<HeroQuote>,
    val heroCaptions: List<Int>,
)

/**
 * One-time events emitted by [GreetingViewModel]; consumed exactly once by the UI.
 */
sealed interface GreetingEvent {
    /** Show a transient toast carrying a string resource. */
    data class ShowToast(@StringRes val messageRes: Int) : GreetingEvent
}

/**
 * Actions sent from the UI to [GreetingViewModel] via [BaseViewModel.trySendAction].
 */
sealed interface GreetingAction {

    data class TabSelected(val tab: NavigationTab) : GreetingAction
    data object SidebarOpened : GreetingAction
    data object SidebarClosed : GreetingAction
    data object SidebarToggled : GreetingAction

    data object InspectorShown : GreetingAction
    data object InspectorDismissed : GreetingAction
    data class PrimaryColorOverridden(val color: Color) : GreetingAction

    data object NextGreetingClicked : GreetingAction
    data class CustomGreetingChanged(val part1: String, val part2: String) : GreetingAction

    data class ThemeSelected(val palette: CssVariables) : GreetingAction
    data class ColorModeChanged(val mode: ColorMode) : GreetingAction
    data class SystemDarkModeChanged(val isDark: Boolean) : GreetingAction

    data class TypographySelected(val choice: AppTypographyChoice) : GreetingAction
    data class CustomFontSelected(val fontId: String) : GreetingAction
    data class FontDownloadClicked(val preset: PresetFont) : GreetingAction
    data class FontDeleteClicked(val fontId: String) : GreetingAction
    data class FontImportRequested(val uri: Uri, val fallbackName: String) : GreetingAction
    data class FontScaleSaved(val scale: Float) : GreetingAction

    data object SettingsMenuOpened : GreetingAction
    data object AppearanceSettingsOpened : GreetingAction
    data object LanguageSettingsOpened : GreetingAction
    data object FontSettingsOpened : GreetingAction
    data object FontSizeSettingsOpened : GreetingAction
    data object SettingsBackPressed : GreetingAction
    data object SettingsExited : GreetingAction

    /**
     * Internal actions: results of asynchronous work posted back onto the action
     * channel so that all state mutations stay synchronous inside [handleAction].
     */
    sealed interface Internal : GreetingAction {
        data class PreferencesReceived(val preferences: UserPreferences) : Internal
        data class InstalledFontsReceived(val fonts: List<InstalledFont>) : Internal
        data class DownloadProgressReceived(val progress: Map<String, Float>) : Internal
        data class FontDownloadCompleted(val success: Boolean) : Internal
        data class FontImportCompleted(val fontId: String?) : Internal
    }
}

/**
 * Resolves the effective [CssVariables] from the raw theme inputs.
 */
private fun resolveTheme(
    themeId: String,
    colorMode: ColorMode,
    primaryOverride: Color?,
    isSystemDark: Boolean,
): CssVariables {
    val family = ThemeResolver.familyOf(themeId)
    val effectiveIsDark = when (colorMode) {
        ColorMode.LIGHT -> false
        ColorMode.DARK -> true
        ColorMode.SYSTEM -> isSystemDark
    }
    val base = ThemeResolver.resolveFamily(family, effectiveIsDark)
    return if (primaryOverride != null) {
        base.copy(primary = primaryOverride, ring = primaryOverride, accent = primaryOverride)
    } else {
        base
    }
}

/**
 * Single ViewModel backing the greeting feature (MVVM + unidirectional data flow).
 *
 * The UI renders [stateFlow] and sends every user intent as a [GreetingAction];
 * one-shot feedback (toasts) is delivered through [eventFlow]. State mutations
 * happen synchronously inside [handleAction]; asynchronous work (persistence,
 * font downloads) posts follow-up [GreetingAction.Internal] actions.
 */
@HiltViewModel
class GreetingViewModel @Inject constructor(
    @ApplicationContext appContext: Context,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val customFontRepository: CustomFontRepository,
    greetingRepository: GreetingRepository,
) : BaseViewModel<GreetingState, GreetingEvent, GreetingAction>(
    initialState = run {
        val isSystemDark =
            (appContext.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES
        GreetingState(
            themeId = UserPreferences.DEFAULT.themeId,
            colorMode = ColorMode.fromId(UserPreferences.DEFAULT.colorMode),
            primaryOverride = null,
            isSystemDark = isSystemDark,
            theme = resolveTheme(
                themeId = UserPreferences.DEFAULT.themeId,
                colorMode = ColorMode.fromId(UserPreferences.DEFAULT.colorMode),
                primaryOverride = null,
                isSystemDark = isSystemDark,
            ),
            activeContentFont = AppTypographyChoice.EDITORIAL.font,
            currentTab = NavigationTab.CANVAS,
            isSidebarOpen = false,
            isInspectorVisible = false,
            settingsLevel = SettingsLevel.NONE,
            typographyChoice = AppTypographyChoice.EDITORIAL,
            fontScale = UserPreferences.DEFAULT.fontScale,
            activeCustomFontId = UserPreferences.DEFAULT.activeCustomFontId,
            installedFonts = emptyList(),
            downloadProgress = emptyMap(),
            greetingIndex = 0,
            customGreeting = CustomGreetingState(),
            heroQuotes = greetingRepository.heroQuotes,
            heroCaptions = greetingRepository.heroCaptions,
        )
    },
) {

    init {
        userPreferencesRepository
            .preferencesStateFlow
            .map { GreetingAction.Internal.PreferencesReceived(it) }
            .onEach(::sendAction)
            .launchIn(viewModelScope)

        customFontRepository
            .installedVersion
            .map { GreetingAction.Internal.InstalledFontsReceived(customFontRepository.installedFonts()) }
            .onEach(::sendAction)
            .launchIn(viewModelScope)

        customFontRepository
            .downloadProgress
            .map { GreetingAction.Internal.DownloadProgressReceived(it) }
            .onEach(::sendAction)
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: GreetingAction) {
        when (action) {
            is GreetingAction.TabSelected -> updateState { copy(currentTab = action.tab) }
            GreetingAction.SidebarOpened -> updateState { copy(isSidebarOpen = true) }
            GreetingAction.SidebarClosed -> updateState { copy(isSidebarOpen = false) }
            GreetingAction.SidebarToggled -> updateState { copy(isSidebarOpen = !isSidebarOpen) }

            GreetingAction.InspectorShown -> updateState { copy(isInspectorVisible = true) }
            GreetingAction.InspectorDismissed -> updateState { copy(isInspectorVisible = false) }
            is GreetingAction.PrimaryColorOverridden -> {
                updateState { copy(primaryOverride = action.color) }
            }

            GreetingAction.NextGreetingClicked -> handleNextGreetingClicked()
            is GreetingAction.CustomGreetingChanged -> {
                updateState {
                    copy(
                        customGreeting = CustomGreetingState(
                            part1 = action.part1,
                            part2 = action.part2,
                            isActive = true,
                        ),
                    )
                }
            }

            is GreetingAction.ThemeSelected -> handleThemeSelected(action)
            is GreetingAction.ColorModeChanged -> handleColorModeChanged(action)
            is GreetingAction.SystemDarkModeChanged -> {
                if (state.isSystemDark != action.isDark) {
                    updateState { copy(isSystemDark = action.isDark) }
                }
            }

            is GreetingAction.TypographySelected -> handleTypographySelected(action)
            is GreetingAction.CustomFontSelected -> handleCustomFontSelected(action)
            is GreetingAction.FontDownloadClicked -> handleFontDownloadClicked(action)
            is GreetingAction.FontDeleteClicked -> handleFontDeleteClicked(action)
            is GreetingAction.FontImportRequested -> handleFontImportRequested(action)
            is GreetingAction.FontScaleSaved -> handleFontScaleSaved(action)

            GreetingAction.SettingsMenuOpened -> {
                updateState { copy(settingsLevel = SettingsLevel.MENU) }
            }
            GreetingAction.AppearanceSettingsOpened -> {
                updateState { copy(settingsLevel = SettingsLevel.PAGE) }
            }
            GreetingAction.LanguageSettingsOpened -> {
                updateState { copy(settingsLevel = SettingsLevel.LANGUAGE) }
            }
            GreetingAction.FontSettingsOpened -> {
                updateState { copy(settingsLevel = SettingsLevel.FONT) }
            }
            GreetingAction.FontSizeSettingsOpened -> {
                updateState { copy(settingsLevel = SettingsLevel.FONT_SIZE) }
            }
            GreetingAction.SettingsBackPressed -> handleSettingsBackPressed()
            GreetingAction.SettingsExited -> {
                updateState { copy(settingsLevel = SettingsLevel.NONE) }
            }

            is GreetingAction.Internal.PreferencesReceived -> handlePreferencesReceived(action)
            is GreetingAction.Internal.InstalledFontsReceived -> {
                updateState { copy(installedFonts = action.fonts) }
            }
            is GreetingAction.Internal.DownloadProgressReceived -> {
                updateState { copy(downloadProgress = action.progress) }
            }
            is GreetingAction.Internal.FontDownloadCompleted -> handleFontDownloadCompleted(action)
            is GreetingAction.Internal.FontImportCompleted -> handleFontImportCompleted(action)
        }
    }

    // region Action handlers

    /** Cycles to the next curated statement, deactivating any custom greeting. */
    private fun handleNextGreetingClicked() {
        updateState {
            copy(
                customGreeting = if (customGreeting.isActive) {
                    customGreeting.copy(isActive = false)
                } else {
                    customGreeting
                },
                greetingIndex = (greetingIndex + 1) % heroQuotes.size,
            )
        }
    }

    private fun handleThemeSelected(action: GreetingAction.ThemeSelected) {
        updateState { copy(primaryOverride = null, themeId = action.palette.themeId) }
        viewModelScope.launch { userPreferencesRepository.updateTheme(action.palette.themeId) }
    }

    private fun handleColorModeChanged(action: GreetingAction.ColorModeChanged) {
        updateState { copy(colorMode = action.mode) }
        viewModelScope.launch { userPreferencesRepository.updateColorMode(action.mode.id) }
    }

    private fun handleTypographySelected(action: GreetingAction.TypographySelected) {
        // Selecting a system engine clears any custom-font override.
        updateState { copy(typographyChoice = action.choice, activeCustomFontId = "") }
        viewModelScope.launch {
            userPreferencesRepository.updateTypography(action.choice.name)
            userPreferencesRepository.updateActiveCustomFont("")
        }
    }

    private fun handleCustomFontSelected(action: GreetingAction.CustomFontSelected) {
        updateState { copy(activeCustomFontId = action.fontId) }
        viewModelScope.launch { userPreferencesRepository.updateActiveCustomFont(action.fontId) }
    }

    private fun handleFontDownloadClicked(action: GreetingAction.FontDownloadClicked) {
        viewModelScope.launch {
            val success = customFontRepository.downloadPreset(action.preset)
            sendAction(GreetingAction.Internal.FontDownloadCompleted(success))
        }
    }

    private fun handleFontDeleteClicked(action: GreetingAction.FontDeleteClicked) {
        if (state.activeCustomFontId == action.fontId) {
            updateState { copy(activeCustomFontId = "") }
            viewModelScope.launch { userPreferencesRepository.updateActiveCustomFont("") }
        }
        customFontRepository.deleteFont(action.fontId)
        sendEvent(GreetingEvent.ShowToast(R.string.font_deleted_toast))
    }

    private fun handleFontImportRequested(action: GreetingAction.FontImportRequested) {
        viewModelScope.launch {
            val fontId = customFontRepository.importFont(action.uri, action.fallbackName)
            sendAction(GreetingAction.Internal.FontImportCompleted(fontId))
        }
    }

    private fun handleFontScaleSaved(action: GreetingAction.FontScaleSaved) {
        updateState { copy(fontScale = action.scale) }
        viewModelScope.launch { userPreferencesRepository.updateFontScale(action.scale) }
    }

    /** Steps one settings level back (PAGE -> MENU -> NONE). */
    private fun handleSettingsBackPressed() {
        updateState {
            copy(
                settingsLevel = when (settingsLevel) {
                    SettingsLevel.PAGE,
                    SettingsLevel.LANGUAGE,
                    SettingsLevel.FONT,
                    -> SettingsLevel.MENU
                    SettingsLevel.FONT_SIZE -> SettingsLevel.FONT
                    SettingsLevel.MENU,
                    SettingsLevel.NONE,
                    -> SettingsLevel.NONE
                },
            )
        }
    }

    // endregion

    // region Internal action handlers

    private fun handlePreferencesReceived(action: GreetingAction.Internal.PreferencesReceived) {
        val prefs = action.preferences
        updateState {
            copy(
                themeId = prefs.themeId,
                colorMode = ColorMode.fromId(prefs.colorMode),
                typographyChoice = AppTypographyChoice.entries
                    .firstOrNull { it.name == prefs.typographyChoice }
                    ?: AppTypographyChoice.EDITORIAL,
                fontScale = prefs.fontScale,
                activeCustomFontId = prefs.activeCustomFontId,
            )
        }
    }

    private fun handleFontDownloadCompleted(action: GreetingAction.Internal.FontDownloadCompleted) {
        sendEvent(
            GreetingEvent.ShowToast(
                if (action.success) {
                    R.string.font_download_complete_toast
                } else {
                    R.string.font_download_failed_toast
                },
            ),
        )
    }

    private fun handleFontImportCompleted(action: GreetingAction.Internal.FontImportCompleted) {
        val fontId = action.fontId
        if (fontId != null) {
            updateState { copy(activeCustomFontId = fontId) }
            viewModelScope.launch { userPreferencesRepository.updateActiveCustomFont(fontId) }
            sendEvent(GreetingEvent.ShowToast(R.string.font_imported_toast))
        } else {
            sendEvent(GreetingEvent.ShowToast(R.string.font_import_failed_toast))
        }
    }

    // endregion

    /**
     * Updates [mutableStateFlow] and re-derives the derived fields ([GreetingState.theme],
     * [GreetingState.activeContentFont]) so they always stay consistent with the raw inputs.
     */
    private inline fun updateState(block: GreetingState.() -> GreetingState) {
        mutableStateFlow.update { current ->
            val next = current.block()
            next.copy(
                theme = resolveTheme(
                    themeId = next.themeId,
                    colorMode = next.colorMode,
                    primaryOverride = next.primaryOverride,
                    isSystemDark = next.isSystemDark,
                ),
                activeContentFont = customFontRepository.fontFamilyFor(next.activeCustomFontId)
                    ?: next.typographyChoice.font,
            )
        }
    }

    /** Resolves an installed custom font to a [FontFamily] for UI previews. */
    fun customFontFamily(fontId: String): FontFamily? = customFontRepository.fontFamilyFor(fontId)
}
