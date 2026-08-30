package com.example.feature.greeting.impl

import android.content.Context
import android.content.res.Configuration
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.data.model.ColorMode
import com.example.core.data.model.UserPreferences
import com.example.core.data.repository.GreetingRepository
import com.example.core.data.repository.HeroQuote
import com.example.core.data.repository.UserPreferencesRepository
import com.example.core.ui.theme.CssVariables
import com.example.core.ui.theme.ThemeResolver
import com.example.feature.greeting.impl.components.NavigationTab
import com.example.feature.greeting.impl.screens.AppTypographyChoice
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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
enum class SettingsLevel { NONE, MENU, PAGE, LANGUAGE }

/**
 * Single ViewModel backing the greeting feature (MVVM).
 *
 * Owns navigation-independent UI state (theme, typography, tab, sidebar,
 * inspector, greeting content) and persists user preferences through the
 * data layer.
 */
@HiltViewModel
class GreetingViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val userPreferencesRepository: UserPreferencesRepository,
    greetingRepository: GreetingRepository,
) : ViewModel() {

    val heroQuotes: List<HeroQuote> = greetingRepository.heroQuotes
    val heroCaptions: List<Int> = greetingRepository.heroCaptions

    private val _currentTab = MutableStateFlow(NavigationTab.CANVAS)
    val currentTab: StateFlow<NavigationTab> = _currentTab.asStateFlow()

    private val _isSidebarOpen = MutableStateFlow(false)
    val isSidebarOpen: StateFlow<Boolean> = _isSidebarOpen.asStateFlow()

    private val _isInspectorVisible = MutableStateFlow(false)
    val isInspectorVisible: StateFlow<Boolean> = _isInspectorVisible.asStateFlow()

    private val _themeId = MutableStateFlow(UserPreferences.DEFAULT.themeId)
    private val _colorMode = MutableStateFlow(ColorMode.fromId(UserPreferences.DEFAULT.colorMode))
    private val _primaryOverride = MutableStateFlow<Color?>(null)

    // System dark-mode state, seeded from the app configuration and kept in
    // sync by the UI layer; it drives the SYSTEM color mode.
    private val _isSystemDark = MutableStateFlow(
        (appContext.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
    )

    val colorMode: StateFlow<ColorMode> = _colorMode.asStateFlow()

    val currentTheme: StateFlow<CssVariables> =
        combine(_themeId, _colorMode, _primaryOverride, _isSystemDark) { themeId, colorMode, primaryOverride, isSystemDark ->
            val family = ThemeResolver.familyOf(themeId)
            val effectiveIsDark = when (colorMode) {
                ColorMode.LIGHT -> false
                ColorMode.DARK -> true
                ColorMode.SYSTEM -> isSystemDark
            }
            val base = ThemeResolver.resolveFamily(family, effectiveIsDark)
            if (primaryOverride != null) {
                base.copy(primary = primaryOverride, ring = primaryOverride, accent = primaryOverride)
            } else {
                base
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ThemeResolver.fromThemeId(UserPreferences.DEFAULT.themeId),
        )

    private val _typographyChoice = MutableStateFlow(AppTypographyChoice.EDITORIAL)
    val typographyChoice: StateFlow<AppTypographyChoice> = _typographyChoice.asStateFlow()

    private val _fontScale = MutableStateFlow(UserPreferences.DEFAULT.fontScale)
    val fontScale: StateFlow<Float> = _fontScale.asStateFlow()

    private val _greetingIndex = MutableStateFlow(0)
    val greetingIndex: StateFlow<Int> = _greetingIndex.asStateFlow()

    private val _customGreeting = MutableStateFlow(CustomGreetingState())
    val customGreeting: StateFlow<CustomGreetingState> = _customGreeting.asStateFlow()

    private val _settingsLevel = MutableStateFlow(SettingsLevel.NONE)
    val settingsLevel: StateFlow<SettingsLevel> = _settingsLevel.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferencesRepository.observePreferences().collect { prefs ->
                _themeId.value = prefs.themeId
                _colorMode.value = ColorMode.fromId(prefs.colorMode)
                _typographyChoice.value = AppTypographyChoice.entries
                    .firstOrNull { it.name == prefs.typographyChoice }
                    ?: AppTypographyChoice.EDITORIAL
                _fontScale.value = prefs.fontScale
            }
        }
    }

    fun selectTab(tab: NavigationTab) {
        _currentTab.value = tab
    }

    fun openSidebar() {
        _isSidebarOpen.value = true
    }

    fun closeSidebar() {
        _isSidebarOpen.value = false
    }

    fun toggleSidebar() {
        _isSidebarOpen.value = !_isSidebarOpen.value
    }

    fun showInspector() {
        _isInspectorVisible.value = true
    }

    fun hideInspector() {
        _isInspectorVisible.value = false
    }

    /** Cycles to the next curated statement, deactivating any custom greeting. */
    fun nextGreeting() {
        if (_customGreeting.value.isActive) {
            _customGreeting.value = _customGreeting.value.copy(isActive = false)
        }
        _greetingIndex.value = (_greetingIndex.value + 1) % heroQuotes.size
    }

    /** Live-updates the custom greeting while the user types. */
    fun updateCustomGreeting(part1: String, part2: String) {
        _customGreeting.value = CustomGreetingState(part1 = part1, part2 = part2, isActive = true)
    }

    fun selectTypography(choice: AppTypographyChoice) {
        _typographyChoice.value = choice
        viewModelScope.launch { userPreferencesRepository.updateTypography(choice.name) }
    }

    /** Sets the app-wide font scale and persists it. */
    fun setFontScale(scale: Float) {
        _fontScale.value = scale
        viewModelScope.launch { userPreferencesRepository.updateFontScale(scale) }
    }

    fun selectTheme(palette: CssVariables) {
        _primaryOverride.value = null
        _themeId.value = palette.themeId
        viewModelScope.launch { userPreferencesRepository.updateTheme(palette.themeId) }
    }

    /** Sets the color mode (follow-system / light / dark) and persists it. */
    fun setColorMode(mode: ColorMode) {
        _colorMode.value = mode
        viewModelScope.launch { userPreferencesRepository.updateColorMode(mode.id) }
    }

    /** Feeds the current system dark-mode state (drives the SYSTEM color mode). */
    fun setSystemDarkMode(isDark: Boolean) {
        if (_isSystemDark.value != isDark) {
            _isSystemDark.value = isDark
        }
    }

    /** Transient primary-color override from the CSS inspector (not persisted). */
    fun overridePrimary(color: Color) {
        _primaryOverride.value = color
    }

    fun openSettingsMenu() {
        _settingsLevel.value = SettingsLevel.MENU
    }

    fun openAppearanceSettings() {
        _settingsLevel.value = SettingsLevel.PAGE
    }

    fun openLanguageSettings() {
        _settingsLevel.value = SettingsLevel.LANGUAGE
    }

    /** Steps one settings level back (PAGE -> MENU -> NONE). */
    fun backSettings() {
        when (_settingsLevel.value) {
            SettingsLevel.PAGE -> _settingsLevel.value = SettingsLevel.MENU
            SettingsLevel.LANGUAGE -> _settingsLevel.value = SettingsLevel.MENU
            SettingsLevel.MENU -> _settingsLevel.value = SettingsLevel.NONE
            SettingsLevel.NONE -> Unit
        }
    }

    /** Exits the settings flow entirely (e.g. when a bottom-nav tab is tapped). */
    fun exitSettings() {
        _settingsLevel.value = SettingsLevel.NONE
    }
}
