package com.example.ui.components

import android.content.Context
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CssVariables
import com.example.ui.theme.ProductionPalettes

/**
 * Top-tier production header / top navigation bar adhering to Linear, Vercel, and Apple HIG standards:
 * - Brand Mark / Monogram & Minimal Version
 * - Status Micro-Pulse
 * - Direct Settings Entry Button (Accessing Full Color Schemes & Appearance Settings)
 * - Quick Theme Mode Segmented Pill / Palette Trigger
 */
@Composable
fun ProductionTopNavBar(
    currentTheme: CssVariables,
    onThemeChange: (CssVariables) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenInspector: () -> Unit,
    onOpenSidebar: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Palette family identifier
    val currentPresetBase = when {
        currentTheme.themeId.startsWith("editorial") -> "Editorial"
        currentTheme.themeId.startsWith("geist") -> "Geist"
        currentTheme.themeId.startsWith("linear") -> "Linear"
        currentTheme.themeId.startsWith("shadcn") -> "Shadcn"
        currentTheme.themeId.startsWith("notion") -> "Notion"
        else -> "Braun"
    }

    // Breathing pulse for live indicator
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Column(
        modifier = modifier
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
            // Left Section: Sidebar Toggle, Brand Monogram, Title, Status Pulse & Version Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Sidebar / Drawer Menu Toggle Button
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(currentTheme.radiusSm))
                        .background(currentTheme.subtleSurface)
                        .border(1.dp, currentTheme.border, RoundedCornerShape(currentTheme.radiusSm))
                        .clickable { onOpenSidebar() }
                        .testTag("top_nav_sidebar_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Menu,
                        contentDescription = "Open Sidebar Menu",
                        tint = currentTheme.foreground,
                        modifier = Modifier.size(15.dp)
                    )
                }

                // Minimal Monogram Glyph Box
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(currentTheme.radiusSm))
                        .background(currentTheme.card)
                        .border(1.dp, currentTheme.border, RoundedCornerShape(currentTheme.radiusSm)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "H",
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = currentTheme.foreground
                    )
                }

                Text(
                    text = "HELLO",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = currentTheme.foreground
                )

                // Micro Live Status Dot & Version Tag
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(currentTheme.radiusSm))
                        .background(currentTheme.subtleSurface)
                        .border(0.5.dp, currentTheme.border.copy(alpha = 0.6f), RoundedCornerShape(currentTheme.radiusSm))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(4.5.dp)
                            .clip(CircleShape)
                            .background(currentTheme.primary.copy(alpha = pulseAlpha))
                    )
                    Text(
                        text = "v1.0",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        color = currentTheme.mutedForeground
                    )
                }
            }

            // Right Section: Settings Entry Button & Quick Actions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // 1. Quick Color Palette Badge (Tapping opens Settings)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(currentTheme.radiusSm))
                        .background(currentTheme.card)
                        .border(1.dp, currentTheme.border, RoundedCornerShape(currentTheme.radiusSm))
                        .clickable { onOpenSettings() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("top_nav_theme_summary_badge")
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(currentTheme.primary)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = currentPresetBase,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = currentTheme.foreground
                    )
                }

                // 2. Settings Button
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(currentTheme.radiusSm))
                        .background(currentTheme.card)
                        .border(1.dp, currentTheme.border, RoundedCornerShape(currentTheme.radiusSm))
                        .clickable { onOpenSettings() }
                        .testTag("top_nav_settings_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Settings",
                        tint = currentTheme.foreground,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }

        // 1px Subtle Bottom Border Rule (Linear / Vercel style)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(currentTheme.border)
        )
    }
}
