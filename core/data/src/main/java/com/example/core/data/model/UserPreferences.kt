package com.example.core.data.model

/**
 * Domain model for persisted UI preferences.
 *
 * Also carries the navigation chrome state ([currentTab], [settingsLevel],
 * [isSidebarOpen]) so the user's location inside the feature survives
 * process death. Enum values are stored by their `name` ids; the feature
 * layer owns the mapping back to its enums.
 */
data class UserPreferences(
    val themeId: String,
    val typographyChoice: String,
    val colorMode: String,
    val fontScale: Float,
    val activeCustomFontId: String,
    val currentTab: String,
    val settingsLevel: String,
    val isSidebarOpen: Boolean,
) {
    companion object {
        val DEFAULT = UserPreferences(
            themeId = "editorial-light",
            typographyChoice = "EDITORIAL",
            colorMode = ColorMode.SYSTEM.id,
            fontScale = 1.0f,
            activeCustomFontId = "",
            currentTab = "CANVAS",
            settingsLevel = "NONE",
            isSidebarOpen = false,
        )
    }
}
