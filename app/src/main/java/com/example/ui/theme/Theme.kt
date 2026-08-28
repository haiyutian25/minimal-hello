package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun MinimalTheme(
    cssVars: CssVariables = ProductionPalettes.GeistDark,
    content: @Composable () -> Unit
) {
    val m3ColorScheme = if (cssVars.isDark) {
        darkColorScheme(
            primary = cssVars.primary,
            onPrimary = cssVars.primaryForeground,
            primaryContainer = cssVars.accent,
            onPrimaryContainer = cssVars.accentForeground,
            secondary = cssVars.mutedForeground,
            onSecondary = cssVars.foreground,
            background = cssVars.background,
            onBackground = cssVars.foreground,
            surface = cssVars.card,
            onSurface = cssVars.cardForeground,
            surfaceVariant = cssVars.subtleSurface,
            onSurfaceVariant = cssVars.mutedForeground,
            outline = cssVars.border,
            outlineVariant = cssVars.muted
        )
    } else {
        lightColorScheme(
            primary = cssVars.primary,
            onPrimary = cssVars.primaryForeground,
            primaryContainer = cssVars.accent,
            onPrimaryContainer = cssVars.accentForeground,
            secondary = cssVars.mutedForeground,
            onSecondary = cssVars.foreground,
            background = cssVars.background,
            onBackground = cssVars.foreground,
            surface = cssVars.card,
            onSurface = cssVars.cardForeground,
            surfaceVariant = cssVars.subtleSurface,
            onSurfaceVariant = cssVars.mutedForeground,
            outline = cssVars.border,
            outlineVariant = cssVars.muted
        )
    }

    CompositionLocalProvider(LocalCssVariables provides cssVars) {
        MaterialTheme(
            colorScheme = m3ColorScheme,
            typography = Typography,
            content = content
        )
    }
}

