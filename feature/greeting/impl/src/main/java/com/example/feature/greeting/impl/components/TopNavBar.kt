package com.example.feature.greeting.impl.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.ui.theme.CssVariables

/**
 * Minimal top navigation bar with two shapes:
 * - Main shape ([pageTitle] == null): sidebar toggle button on the left.
 * - Sub-page shape ([pageTitle] != null, e.g. settings flow): back button on
 *   the left + centered page title.
 * Always finished with the 1px bottom divider rule.
 */
@Composable
fun ProductionTopNavBar(
    currentTheme: CssVariables,
    onOpenSidebar: () -> Unit = {},
    pageTitle: String? = null,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(currentTheme.background)
            .statusBarsPadding()
    ) {
        if (pageTitle != null) {
            // Sub-page shape: back button (left) + centered title
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .padding(horizontal = 10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(28.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onBack() }
                        .testTag("top_nav_back_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = currentTheme.foreground,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = pageTitle,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.2).sp,
                    color = currentTheme.foreground,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        } else {
            // Main shape: sidebar toggle button (plain icon, no ripple)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onOpenSidebar() }
                        .testTag("top_nav_sidebar_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Menu,
                        contentDescription = "Open Sidebar Menu",
                        tint = currentTheme.foreground,
                        modifier = Modifier.size(28.dp)
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
