package com.example.feature.greeting.impl.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.outlined.Contrast
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.data.model.ColorMode
import com.example.core.ui.theme.CssVariables
import com.example.core.ui.theme.ProductionPalettes
import com.example.core.ui.theme.isBraun
import com.example.feature.greeting.impl.R
import com.example.feature.greeting.impl.components.Button
import com.example.feature.greeting.impl.components.CardButton

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
    colorMode: ColorMode,
    onColorModeChange: (ColorMode) -> Unit,
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

            // Three-way Color Mode Selector: Follow System / Light / Dark
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ColorModeCard(
                    icon = Icons.Default.AutoAwesome,
                    title = stringResource(R.string.settings_mode_system_title),
                    isSelected = colorMode == ColorMode.SYSTEM,
                    currentTheme = currentTheme,
                    testTag = "settings_mode_system_card",
                    modifier = Modifier.weight(1f),
                    onClick = { onColorModeChange(ColorMode.SYSTEM) }
                )
                ColorModeCard(
                    icon = Icons.Default.LightMode,
                    title = stringResource(R.string.settings_mode_light_short),
                    isSelected = colorMode == ColorMode.LIGHT,
                    currentTheme = currentTheme,
                    testTag = "settings_mode_light_card",
                    modifier = Modifier.weight(1f),
                    onClick = { onColorModeChange(ColorMode.LIGHT) }
                )
                ColorModeCard(
                    icon = Icons.Default.DarkMode,
                    title = stringResource(R.string.settings_mode_dark_short),
                    isSelected = colorMode == ColorMode.DARK,
                    currentTheme = currentTheme,
                    testTag = "settings_mode_dark_card",
                    modifier = Modifier.weight(1f),
                    onClick = { onColorModeChange(ColorMode.DARK) }
                )
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

@Composable
private fun ColorModeCard(
    icon: ImageVector,
    title: String,
    isSelected: Boolean,
    currentTheme: CssVariables,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    CardButton(
        onClick = onClick,
        isSelected = isSelected,
        currentTheme = currentTheme,
        modifier = modifier,
        shape = RoundedCornerShape(currentTheme.radiusMd),
        // Selected card gets an elevated surface (white in light / muted in dark)
        // instead of staying the same gray as the unselected cards. Braun (Dieter
        // Rams) keeps its original restrained card surface instead.
        containerColor = if (isSelected) {
            when {
                currentTheme.isBraun -> currentTheme.card
                currentTheme.isDark -> currentTheme.muted
                else -> Color.White
            }
        } else {
            currentTheme.card
        },
        contentPadding = PaddingValues(vertical = 14.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center,
        testTag = testTag
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) currentTheme.primary else currentTheme.mutedForeground,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(7.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) currentTheme.foreground else currentTheme.mutedForeground
            )
        }
    }
}
