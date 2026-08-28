package com.example.feature.greeting.impl.screens

import com.example.core.ui.util.copyToClipboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.ui.theme.CssVariables
import com.example.core.ui.theme.ProductionPalettes
import com.example.core.ui.theme.toHex
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class AppTypographyChoice(val title: String, val subtitle: String, val font: FontFamily) {
    EDITORIAL("Editorial Serif", "Playfair / Georgia style", FontFamily.Serif),
    SANS("Modern Sans", "Inter / SF Pro style", FontFamily.SansSerif),
    MONO("Technical Mono", "Geist Mono / Fira style", FontFamily.Monospace)
}

@Composable
fun SettingsScreen(
    currentTheme: CssVariables,
    onThemeChange: (CssVariables) -> Unit,
    selectedTypography: AppTypographyChoice,
    onTypographyChange: (AppTypographyChoice) -> Unit,
    onBack: () -> Unit,
    onOpenInspector: () -> Unit,
    onReplaySplash: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isCopied by remember { mutableStateOf(false) }

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
        Triple("Editorial Aesthetic", "Warm paper & classical serif elegance", ProductionPalettes.EditorialLight to ProductionPalettes.EditorialDark),
        Triple("Geist Minimal", "Vercel high-contrast pure monochrome", ProductionPalettes.GeistLight to ProductionPalettes.GeistDark),
        Triple("Linear Obsidian", "Deep space indigo & precise engineering", ProductionPalettes.LinearLight to ProductionPalettes.LinearDark),
        Triple("Shadcn Zinc", "Balanced zinc neutrals & modern UI", ProductionPalettes.ShadcnZincLight to ProductionPalettes.ShadcnZincDark),
        Triple("Notion Warm", "Wabi-sabi oat tone & quiet clarity", ProductionPalettes.NotionWarmLight to ProductionPalettes.NotionWarmDark),
        Triple("Braun Dieter Rams", "Industrial orange accent & honest design", ProductionPalettes.DieterRamsLight to ProductionPalettes.DieterRamsDark)
    )

    Scaffold(
        containerColor = currentTheme.background,
        contentColor = currentTheme.foreground,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(currentTheme.background)
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(currentTheme.radiusSm))
                                .testTag("settings_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = currentTheme.foreground,
                                modifier = Modifier.size(17.dp)
                            )
                        }

                        Text(
                            text = "Settings",
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.2).sp,
                            color = currentTheme.foreground
                        )
                    }

                    // Reset / default indicator badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(currentTheme.radiusSm))
                            .background(currentTheme.subtleSurface)
                            .border(1.dp, currentTheme.border.copy(alpha = 0.6f), RoundedCornerShape(currentTheme.radiusSm))
                            .padding(horizontal = 7.dp, vertical = 2.5.dp)
                    ) {
                        Text(
                            text = "Design System",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.5.sp,
                            color = currentTheme.mutedForeground
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(currentTheme.border)
                )
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                title = "COLOR SCHEME & MODE",
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
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(currentTheme.radiusMd))
                        .background(currentTheme.card)
                        .border(
                            width = if (isLight) 2.dp else 1.dp,
                            color = if (isLight) currentTheme.primary else currentTheme.border,
                            shape = RoundedCornerShape(currentTheme.radiusMd)
                        )
                        .clickable {
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
                        }
                        .padding(14.dp)
                        .testTag("settings_mode_light_card")
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
                            text = "Light Canvas",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = currentTheme.foreground
                        )
                        Text(
                            text = "Clean paper tone",
                            fontSize = 11.sp,
                            color = currentTheme.mutedForeground
                        )
                    }
                }

                // Dark Mode Card
                val isDark = currentTheme.isDark
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(currentTheme.radiusMd))
                        .background(currentTheme.card)
                        .border(
                            width = if (isDark) 2.dp else 1.dp,
                            color = if (isDark) currentTheme.primary else currentTheme.border,
                            shape = RoundedCornerShape(currentTheme.radiusMd)
                        )
                        .clickable {
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
                        }
                        .padding(14.dp)
                        .testTag("settings_mode_dark_card")
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
                            text = "Dark / OLED",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = currentTheme.foreground
                        )
                        Text(
                            text = "Deep contrast",
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
                title = "CURATED DESIGN PALETTES",
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
                paletteList.forEachIndexed { index, (name, subtitle, variants) ->
                    val isSelected = currentPresetBase == name.split(" ").first()
                    val targetTheme = if (currentTheme.isDark) variants.second else variants.first

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onThemeChange(targetTheme) }
                            .padding(horizontal = 16.dp, vertical = 13.dp)
                            .testTag("settings_palette_item_$name"),
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
                                    text = name,
                                    fontSize = 13.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = currentTheme.foreground
                                )
                                Text(
                                    text = subtitle,
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
                title = "TYPOGRAPHY ENGINE",
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

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTypographyChange(style) }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .testTag("settings_typography_${style.name}"),
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
                                    text = style.title,
                                    fontFamily = style.font,
                                    fontSize = 13.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = currentTheme.foreground
                                )
                                Text(
                                    text = style.subtitle,
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

            Spacer(modifier = Modifier.height(24.dp))

            // ==========================================
            // SECTION 4: Developer Tools & CSS Variables
            // ==========================================
            SettingsSectionHeader(
                title = "DEVELOPER & DESIGN TOKENS",
                icon = Icons.Outlined.Code,
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
                // Row 1: Copy CSS Root Variables
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            context.copyToClipboard(currentTheme.toCssString(), label = "CSS Variables")
                            isCopied = true
                            Toast.makeText(context, "CSS Tokens Copied to Clipboard", Toast.LENGTH_SHORT).show()
                            scope.launch {
                                delay(1800)
                                isCopied = false
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                        .testTag("settings_copy_css_item"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Code,
                            contentDescription = null,
                            tint = currentTheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "Copy CSS Root Tokens",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = currentTheme.foreground
                            )
                            Text(
                                text = "Export :root { ... } custom properties",
                                fontSize = 11.sp,
                                color = currentTheme.mutedForeground
                            )
                        }
                    }

                    Text(
                        text = if (isCopied) "Copied!" else "Copy",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = currentTheme.primary
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(0.5.dp)
                        .background(currentTheme.border.copy(alpha = 0.5f))
                )

                // Row 2: Open Advanced Inspector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenInspector() }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                        .testTag("settings_open_inspector_item"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Tune,
                            contentDescription = null,
                            tint = currentTheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "Live CSS Variable Inspector",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = currentTheme.foreground
                            )
                            Text(
                                text = "Deep dive into 14+ token overrides & hex preview",
                                fontSize = 11.sp,
                                color = currentTheme.mutedForeground
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = currentTheme.mutedForeground,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ==========================================
            // SECTION 5: System & Benchmark Info
            // ==========================================
            SettingsSectionHeader(
                title = "APPLICATION & SYSTEM INFO",
                icon = Icons.Outlined.Info,
                currentTheme = currentTheme
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(currentTheme.radiusLg))
                    .background(currentTheme.card)
                    .border(1.dp, currentTheme.border, RoundedCornerShape(currentTheme.radiusLg))
                    .padding(16.dp)
            ) {
                InfoRow(label = "Application Version", value = "1.0.0 (Production)", theme = currentTheme)
                Spacer(modifier = Modifier.height(8.dp))
                InfoRow(label = "Design Standard", value = "CSS Custom Tokens (W3C)", theme = currentTheme)
                Spacer(modifier = Modifier.height(8.dp))
                InfoRow(label = "Active Accent", value = "#${currentTheme.primary.toHex()}", theme = currentTheme)
                Spacer(modifier = Modifier.height(8.dp))
                InfoRow(label = "Radius Spec", value = "${currentTheme.radiusLg.value.toInt()}px (Rounded)", theme = currentTheme)

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(currentTheme.radiusSm))
                        .background(currentTheme.subtleSurface)
                        .border(1.dp, currentTheme.border, RoundedCornerShape(currentTheme.radiusSm))
                        .clickable { onReplaySplash() }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .testTag("settings_replay_splash_btn"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.RestartAlt,
                            contentDescription = null,
                            tint = currentTheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Column {
                            Text(
                                text = "Preview Boot Splash Animation",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = currentTheme.foreground
                            )
                            Text(
                                text = "Replay typewriter sequence & kinetic studio intro",
                                fontSize = 11.sp,
                                color = currentTheme.mutedForeground
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = currentTheme.mutedForeground,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
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
private fun InfoRow(
    label: String,
    value: String,
    theme: CssVariables
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = theme.mutedForeground
        )
        Text(
            text = value,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Medium,
            color = theme.foreground
        )
    }
}
