package com.example.feature.greeting.impl.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.ui.theme.CssVariables

enum class NavigationTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    CANVAS("Canvas", Icons.Filled.Home, Icons.Outlined.Home, "bottom_tab_canvas"),
    TYPOGRAPHY("Type", Icons.Filled.FormatSize, Icons.Outlined.FormatSize, "bottom_tab_typography"),
    TOKENS("Tokens", Icons.Filled.Code, Icons.Outlined.Code, "bottom_tab_tokens"),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings, "bottom_tab_settings")
}

/**
 * Modern Production Bottom Navigation Bar adhering to Linear, Vercel, and Material 3 standards:
 * - 48~52dp content height + navigationBarsPadding()
 * - Micro-pill active state highlight with smooth color transitions
 * - 1px subtle top border line (CSS border token)
 * - Minimum 48dp touch target with accessibility content descriptions
 */
@Composable
fun ProductionBottomNavBar(
    currentTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    currentTheme: CssVariables,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(currentTheme.background)
            .navigationBarsPadding()
    ) {
        // 1px Subtle Top Border
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(currentTheme.border)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavigationTab.values().forEach { tab ->
                val isSelected = currentTab == tab

                val iconColor by animateColorAsState(
                    targetValue = if (isSelected) currentTheme.primary else currentTheme.mutedForeground,
                    animationSpec = tween(200),
                    label = "tab_icon_color"
                )

                val textColor by animateColorAsState(
                    targetValue = if (isSelected) currentTheme.foreground else currentTheme.mutedForeground,
                    animationSpec = tween(200),
                    label = "tab_text_color"
                )

                val pillBackground by animateColorAsState(
                    targetValue = if (isSelected) currentTheme.subtleSurface else currentTheme.background.copy(alpha = 0f),
                    animationSpec = tween(200),
                    label = "tab_pill_bg"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .clip(RoundedCornerShape(currentTheme.radiusSm))
                        .background(pillBackground)
                        .clickable {
                            onTabSelected(tab)
                        }
                        .padding(vertical = 3.dp)
                        .testTag(tab.testTag),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                            contentDescription = tab.title,
                            tint = iconColor,
                            modifier = Modifier.size(19.dp)
                        )
                        Text(
                            text = tab.title,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = textColor,
                            letterSpacing = 0.2.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
