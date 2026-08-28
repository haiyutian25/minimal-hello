package com.example.feature.greeting.impl.components

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.ui.theme.CssVariables

/**
 * Minimal sidebar drawer:
 * - Workspace / Profile header (top)
 * - Settings entry pinned to the bottom (moved here from the top nav bar)
 */
@Composable
fun AppSidebarContent(
    currentTheme: CssVariables,
    onOpenSettings: () -> Unit,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(currentTheme.card)
            .border(1.dp, currentTheme.border)
            .statusBarsPadding()
            .navigationBarsPadding()
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
        }

        Spacer(modifier = Modifier.weight(1f))

        // 2. Settings Entry (moved from the top nav bar, pinned to the bottom)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(currentTheme.radiusSm))
                .background(currentTheme.subtleSurface)
                .border(1.dp, currentTheme.border, RoundedCornerShape(currentTheme.radiusSm))
                .clickable {
                    onOpenSettings()
                    onCloseDrawer()
                }
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .testTag("sidebar_settings_btn"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "Settings",
                tint = currentTheme.foreground,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Settings",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = currentTheme.foreground
            )
        }
    }
}
