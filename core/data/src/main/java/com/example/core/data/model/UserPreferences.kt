package com.example.core.data.model

/**
 * Domain model for persisted UI preferences.
 */
data class UserPreferences(
    val themeId: String,
    val typographyChoice: String,
    val colorMode: String,
    val fontScale: Float,
) {
    companion object {
        val DEFAULT = UserPreferences(
            themeId = "editorial-light",
            typographyChoice = "EDITORIAL",
            colorMode = ColorMode.SYSTEM.id,
            fontScale = 1.0f,
        )
    }
}
