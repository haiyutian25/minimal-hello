package com.example.feature.greeting.impl.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.ui.theme.CssVariables
import com.example.core.ui.theme.ProductionPalettes
import com.example.feature.greeting.impl.R
import com.example.feature.greeting.impl.components.Button
import com.example.feature.greeting.impl.components.CardButton

enum class AppTypographyChoice(@StringRes val titleRes: Int, @StringRes val subtitleRes: Int, val font: FontFamily) {
    EDITORIAL(R.string.settings_typography_editorial_title, R.string.settings_typography_editorial_subtitle, FontFamily.Serif),
    SANS(R.string.settings_typography_sans_title, R.string.settings_typography_sans_subtitle, FontFamily.SansSerif),
    MONO(R.string.settings_typography_mono_title, R.string.settings_typography_mono_subtitle, FontFamily.Monospace)
}

private data class PaletteEntry(
    val baseKey: String,
    @StringRes val nameRes: Int,
    @StringRes val subtitleRes: Int,
    val light: CssVariables,
    val dark: CssVariables
)

@Composable
fun SettingsScreen(
    currentTheme: CssVariables,
    onThemeChange: (CssVariables) -> Unit,
    selectedTypography: AppTypographyChoice,
    onTypographyChange: (AppTypographyChoice) -> Unit,
    modifier: Modifier = Modifier
) {
    // Identify current palette base
    val currentPresetBase = when {
        currentTheme.themeId.startsWith("editorial") -> "Editorial"
        currentTheme.themeId.startsWith("geist") -> "Geist"
        currentTheme.themeId.startsWith("linear") -> "Linear"
        currentTheme.themeId.startsWith("shadcn") -> "Shadcn"
        currentTheme.themeId.startsWith("notion") -> "Notion"
        else -> "Braun"
    }

    val paletteList = listOf(
        PaletteEntry("Editorial", R.string.settings_palette_editorial_name, R.string.settings_palette_editorial_subtitle, ProductionPalettes.EditorialLight, ProductionPalettes.EditorialDark),
        PaletteEntry("Geist", R.string.settings_palette_geist_name, R.string.settings_palette_geist_subtitle, ProductionPalettes.GeistLight, ProductionPalettes.GeistDark),
        PaletteEntry("Linear", R.string.settings_palette_linear_name, R.string.settings_palette_linear_subtitle, ProductionPalettes.LinearLight, ProductionPalettes.LinearDark),
        PaletteEntry("Shadcn", R.string.settings_palette_shadcn_name, R.string.settings_palette_shadcn_subtitle, ProductionPalettes.ShadcnZincLight, ProductionPalettes.ShadcnZincDark),
        PaletteEntry("Notion", R.string.settings_palette_notion_name, R.string.settings_palette_notion_subtitle, ProductionPalettes.NotionWarmLight, ProductionPalettes.NotionWarmDark),
        PaletteEntry("Braun", R.string.settings_palette_braun_name, R.string.settings_palette_braun_subtitle, ProductionPalettes.DieterRamsLight, ProductionPalettes.DieterRamsDark)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(currentTheme.background)
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp)
            .widthIn(max = 560.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
            // ==========================================
            // SECTION 1: Color Mode (Light / Dark)
            // ==========================================
            SettingsSectionHeader(
                title = stringResource(R.string.settings_section_color_mode),
                icon = Icons.Outlined.Contrast,
                currentTheme = currentTheme
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Dual Card Mode Selector (Apple HIG / Linear settings style)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val isLight = !currentTheme.isDark
                // Light Mode Card
                CardButton(
                    onClick = {
                        if (currentTheme.isDark) {
                            val lightTheme = when (currentPresetBase) {
                                "Editorial" -> ProductionPalettes.EditorialLight
                                "Geist" -> ProductionPalettes.GeistLight
                                "Linear" -> ProductionPalettes.LinearLight
                                "Shadcn" -> ProductionPalettes.ShadcnZincLight
                                "Notion" -> ProductionPalettes.NotionWarmLight
                                else -> ProductionPalettes.DieterRamsLight
                            }
                            onThemeChange(lightTheme)
                        }
                    },
                    isSelected = isLight,
                    currentTheme = currentTheme,
                    modifier = Modifier.weight(1f),
                    testTag = "settings_mode_light_card"
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF7F5F0))
                                    .border(1.dp, Color(0xFFE5E0D8), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LightMode,
                                    contentDescription = null,
                                    tint = Color(0xFF1E1E1E),
                                    modifier = Modifier.size(15.dp)
                                )
                            }

                            if (isLight) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(currentTheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = currentTheme.background,
                                        modifier = Modifier.size(11.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = stringResource(R.string.settings_mode_light_title),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = currentTheme.foreground
                        )
                        Text(
                            text = stringResource(R.string.settings_mode_light_subtitle),
                            fontSize = 11.sp,
                            color = currentTheme.mutedForeground
                        )
                    }
                }

                // Dark Mode Card
                val isDark = currentTheme.isDark
                CardButton(
                    onClick = {
                        if (!currentTheme.isDark) {
                            val darkTheme = when (currentPresetBase) {
                                "Editorial" -> ProductionPalettes.EditorialDark
                                "Geist" -> ProductionPalettes.GeistDark
                                "Linear" -> ProductionPalettes.LinearDark
                                "Shadcn" -> ProductionPalettes.ShadcnZincDark
                                "Notion" -> ProductionPalettes.NotionWarmDark
                                else -> ProductionPalettes.DieterRamsDark
                            }
                            onThemeChange(darkTheme)
                        }
                    },
                    isSelected = isDark,
                    currentTheme = currentTheme,
                    modifier = Modifier.weight(1f),
                    testTag = "settings_mode_dark_card"
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF141414))
                                    .border(1.dp, Color(0xFF2E2E2E), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DarkMode,
                                    contentDescription = null,
                                    tint = Color(0xFFEEEEEE),
                                    modifier = Modifier.size(15.dp)
                                )
                            }

                            if (isDark) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(currentTheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = currentTheme.background,
                                        modifier = Modifier.size(11.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = stringResource(R.string.settings_mode_dark_title),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = currentTheme.foreground
                        )
                        Text(
                            text = stringResource(R.string.settings_mode_dark_subtitle),
                            fontSize = 11.sp,
                            color = currentTheme.mutedForeground
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ==========================================
            // SECTION 2: Production Palette Presets List
            // ==========================================
            SettingsSectionHeader(
                title = stringResource(R.string.settings_section_palettes),
                icon = Icons.Outlined.Palette,
                currentTheme = currentTheme
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Grouped Menu List (iOS / Linear Settings List Pattern)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(currentTheme.radiusLg))
                    .background(currentTheme.card)
                    .border(1.dp, currentTheme.border, RoundedCornerShape(currentTheme.radiusLg))
            ) {
                paletteList.forEachIndexed { index, entry ->
                    val isSelected = currentPresetBase == entry.baseKey
                    val targetTheme = if (currentTheme.isDark) entry.dark else entry.light

                    Button(
                        onClick = { onThemeChange(targetTheme) },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "settings_palette_item_${entry.baseKey}"
                    ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 13.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Dual color dots (Background & Primary Accent)
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(targetTheme.background)
                                    .border(1.dp, targetTheme.border, RoundedCornerShape(4.dp))
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(targetTheme.primary)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(targetTheme.card)
                                        .border(0.5.dp, targetTheme.border, CircleShape)
                                )
                            }

                            Column {
                                Text(
                                    text = stringResource(entry.nameRes),
                                    fontSize = 13.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = currentTheme.foreground
                                )
                                Text(
                                    text = stringResource(entry.subtitleRes),
                                    fontSize = 11.sp,
                                    color = currentTheme.mutedForeground
                                )
                            }
                        }

                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(currentTheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = currentTheme.background,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                    }

                    if (index < paletteList.size - 1) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .height(0.5.dp)
                                .background(currentTheme.border.copy(alpha = 0.5f))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ==========================================
            // SECTION 3: Typography Style
            // ==========================================
            SettingsSectionHeader(
                title = stringResource(R.string.settings_section_typography),
                icon = Icons.Outlined.FormatSize,
                currentTheme = currentTheme
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(currentTheme.radiusLg))
                    .background(currentTheme.card)
                    .border(1.dp, currentTheme.border, RoundedCornerShape(currentTheme.radiusLg))
            ) {
                AppTypographyChoice.values().forEachIndexed { index, style ->
                    val isSelected = selectedTypography == style

                    Button(
                        onClick = { onTypographyChange(style) },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "settings_typography_${style.name}"
                    ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Aa",
                                fontFamily = style.font,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) currentTheme.primary else currentTheme.mutedForeground,
                                modifier = Modifier.width(26.dp)
                            )

                            Column {
                                Text(
                                    text = stringResource(style.titleRes),
                                    fontFamily = style.font,
                                    fontSize = 13.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = currentTheme.foreground
                                )
                                Text(
                                    text = stringResource(style.subtitleRes),
                                    fontSize = 10.5.sp,
                                    color = currentTheme.mutedForeground
                                )
                            }
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = currentTheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    }

                    if (index < AppTypographyChoice.values().size - 1) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .height(0.5.dp)
                                .background(currentTheme.border.copy(alpha = 0.5f))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
}

@Composable
private fun SettingsSectionHeader(
    title: String,
    icon: ImageVector,
    currentTheme: CssVariables
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = currentTheme.primary,
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = title,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color = currentTheme.mutedForeground
        )
    }
}
