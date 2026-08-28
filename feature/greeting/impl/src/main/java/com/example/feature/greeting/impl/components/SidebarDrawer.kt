package com.example.feature.greeting.impl.components

import com.example.core.ui.util.copyToClipboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.ui.theme.CssVariables
import com.example.core.ui.theme.ProductionPalettes
import kotlinx.coroutines.launch

/**
 * Modern Production Sidebar Drawer adhering to Linear, Vercel, and Notion standards:
 * - Workspace / Profile Switcher Header
 * - Grouped Navigation Items with Active Badges
 * - Direct Theme & Mode Switcher within Drawer
 * - CSS Variable Tool Shortcuts
 * - Telemetry & Design Tokens Footer
 */
@Composable
fun AppSidebarContent(
    currentTab: NavigationTab,
    onSelectTab: (NavigationTab) -> Unit,
    currentTheme: CssVariables,
    onThemeChange: (CssVariables) -> Unit,
    onOpenInspector: () -> Unit,
    onCloseDrawer: () -> Unit,
    onReplaySplash: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val currentPresetBase = when {
        currentTheme.themeId.startsWith("editorial") -> "Editorial"
        currentTheme.themeId.startsWith("geist") -> "Geist"
        currentTheme.themeId.startsWith("linear") -> "Linear"
        currentTheme.themeId.startsWith("shadcn") -> "Shadcn"
        currentTheme.themeId.startsWith("notion") -> "Notion"
        else -> "Braun"
    }

    val paletteList = listOf(
        "Editorial" to (ProductionPalettes.EditorialLight to ProductionPalettes.EditorialDark),
        "Geist" to (ProductionPalettes.GeistLight to ProductionPalettes.GeistDark),
        "Linear" to (ProductionPalettes.LinearLight to ProductionPalettes.LinearDark),
        "Shadcn" to (ProductionPalettes.ShadcnZincLight to ProductionPalettes.ShadcnZincDark),
        "Notion" to (ProductionPalettes.NotionWarmLight to ProductionPalettes.NotionWarmDark),
        "Braun" to (ProductionPalettes.DieterRamsLight to ProductionPalettes.DieterRamsDark)
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(currentTheme.card)
            .border(1.dp, currentTheme.border)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag("app_sidebar_drawer")
    ) {
        // 1. Workspace / Profile Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Workspace Avatar
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(currentTheme.radiusSm))
                        .background(currentTheme.subtleSurface)
                        .border(1.dp, currentTheme.border, RoundedCornerShape(currentTheme.radiusSm)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "H",
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = currentTheme.foreground
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Hello Studio",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = currentTheme.foreground
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(currentTheme.primary.copy(alpha = 0.15f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "PRO",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = currentTheme.primary
                            )
                        }
                    }
                    Text(
                        text = "Design Systems Lab",
                        fontSize = 11.sp,
                        color = currentTheme.mutedForeground
                    )
                }
            }

            IconButton(
                onClick = onCloseDrawer,
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(currentTheme.radiusSm))
                    .background(currentTheme.subtleSurface)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Close Sidebar",
                    tint = currentTheme.mutedForeground,
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(currentTheme.border)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Navigation Destination Links
        Text(
            text = "NAVIGATION",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color = currentTheme.mutedForeground,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        NavigationTab.values().forEach { tab ->
            val isSelected = currentTab == tab
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(currentTheme.radiusSm))
                    .background(if (isSelected) currentTheme.subtleSurface else Color.Transparent)
                    .border(
                        width = if (isSelected) 1.dp else 0.dp,
                        color = if (isSelected) currentTheme.border else Color.Transparent,
                        shape = RoundedCornerShape(currentTheme.radiusSm)
                    )
                    .clickable {
                        onSelectTab(tab)
                        onCloseDrawer()
                    }
                    .padding(horizontal = 10.dp, vertical = 9.dp)
                    .testTag("sidebar_item_${tab.name.lowercase()}"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = tab.title,
                        tint = if (isSelected) currentTheme.primary else currentTheme.mutedForeground,
                        modifier = Modifier.size(17.dp)
                    )
                    Text(
                        text = when (tab) {
                            NavigationTab.CANVAS -> "Craft Canvas"
                            NavigationTab.TYPOGRAPHY -> "Typography Studio"
                            NavigationTab.TOKENS -> "CSS Design Tokens"
                            NavigationTab.SETTINGS -> "Settings & Themes"
                        },
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) currentTheme.foreground else currentTheme.foreground.copy(alpha = 0.85f)
                    )
                }

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(currentTheme.primary)
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 3. Fast Theme Switcher Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PALETTES",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = currentTheme.mutedForeground
            )

            // Fast Light/Dark switch icon
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(currentTheme.radiusSm))
                    .background(currentTheme.subtleSurface)
                    .border(0.5.dp, currentTheme.border, RoundedCornerShape(currentTheme.radiusSm))
                    .clickable {
                        val isDark = currentTheme.isDark
                        val targetTheme = when (currentPresetBase) {
                            "Editorial" -> if (isDark) ProductionPalettes.EditorialLight else ProductionPalettes.EditorialDark
                            "Geist" -> if (isDark) ProductionPalettes.GeistLight else ProductionPalettes.GeistDark
                            "Linear" -> if (isDark) ProductionPalettes.LinearLight else ProductionPalettes.LinearDark
                            "Shadcn" -> if (isDark) ProductionPalettes.ShadcnZincLight else ProductionPalettes.ShadcnZincDark
                            "Notion" -> if (isDark) ProductionPalettes.NotionWarmLight else ProductionPalettes.NotionWarmDark
                            else -> if (isDark) ProductionPalettes.DieterRamsLight else ProductionPalettes.DieterRamsDark
                        }
                        onThemeChange(targetTheme)
                    }
                    .padding(horizontal = 6.dp, vertical = 2.5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = if (currentTheme.isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                        contentDescription = null,
                        tint = currentTheme.foreground,
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = if (currentTheme.isDark) "Dark" else "Light",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = currentTheme.foreground
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Grid of 6 Theme Presets in Drawer
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            paletteList.forEach { (name, variants) ->
                val isSelected = currentPresetBase == name
                val targetTheme = if (currentTheme.isDark) variants.second else variants.first

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(currentTheme.radiusSm))
                        .background(if (isSelected) currentTheme.subtleSurface else Color.Transparent)
                        .border(
                            width = if (isSelected) 1.dp else 0.dp,
                            color = if (isSelected) currentTheme.border else Color.Transparent,
                            shape = RoundedCornerShape(currentTheme.radiusSm)
                        )
                        .clickable { onThemeChange(targetTheme) }
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(targetTheme.primary)
                        )
                        Text(
                            text = name,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) currentTheme.foreground else currentTheme.mutedForeground
                        )
                    }

                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = currentTheme.primary,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 4. Quick Tools
        Text(
            text = "QUICK ACTIONS",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color = currentTheme.mutedForeground,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Quick action: Inspect CSS Variables
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(currentTheme.radiusSm))
                .clickable {
                    onOpenInspector()
                    onCloseDrawer()
                }
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Tune,
                contentDescription = null,
                tint = currentTheme.primary,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = "Inspect CSS Tokens",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = currentTheme.foreground
            )
        }

        // Quick action: Copy CSS String
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(currentTheme.radiusSm))
                .clickable {
                    context.copyToClipboard(currentTheme.toCssString(), label = "CSS Variables")
                    Toast.makeText(context, "CSS Variables Copied", Toast.LENGTH_SHORT).show()
                    onCloseDrawer()
                }
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Code,
                contentDescription = null,
                tint = currentTheme.primary,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = "Export :root CSS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = currentTheme.foreground
            )
        }

        // Quick action: Replay Boot Splash Screen
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(currentTheme.radiusSm))
                .clickable {
                    onCloseDrawer()
                    onReplaySplash()
                }
                .padding(horizontal = 10.dp, vertical = 8.dp)
                .testTag("sidebar_replay_splash_btn"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.RestartAlt,
                contentDescription = null,
                tint = currentTheme.primary,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = "Replay Splash Screen",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = currentTheme.foreground
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(16.dp))

        // 5. Drawer Footer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(currentTheme.radiusSm))
                .background(currentTheme.subtleSurface)
                .border(0.5.dp, currentTheme.border, RoundedCornerShape(currentTheme.radiusSm))
                .padding(10.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "ENGINE",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = currentTheme.mutedForeground
                    )
                    Text(
                        text = "Jetpack Compose",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium,
                        color = currentTheme.foreground
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "STANDARD",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = currentTheme.mutedForeground
                    )
                    Text(
                        text = "W3C CSS Level 4",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium,
                        color = currentTheme.foreground
                    )
                }
            }
        }
    }
}
