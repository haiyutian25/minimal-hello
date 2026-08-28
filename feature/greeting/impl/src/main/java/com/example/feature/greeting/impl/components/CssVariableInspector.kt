package com.example.feature.greeting.impl.components

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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.ui.theme.CssTheme
import com.example.core.ui.theme.CssVariables
import com.example.core.ui.theme.toHex
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CssVariableInspectorSheet(
    currentTheme: CssVariables,
    onDismiss: () -> Unit,
    onCustomPrimarySelected: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isCopied by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) } // 0: CSS Code, 1: Variable Swatches

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CssTheme.vars.card,
        contentColor = CssTheme.vars.cardForeground,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(CssTheme.vars.mutedForeground.copy(alpha = 0.4f))
            )
        },
        modifier = modifier.testTag("css_variable_inspector_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "CSS Variables Inspector",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CssTheme.vars.cardForeground,
                        letterSpacing = (-0.3).sp
                    )
                    Text(
                        text = "Tokenized Design System • ${currentTheme.name}",
                        fontSize = 12.sp,
                        color = CssTheme.vars.mutedForeground
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_inspector_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = CssTheme.vars.mutedForeground
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tab switchers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(CssTheme.vars.radiusSm))
                    .background(CssTheme.vars.muted)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TabPill(
                    title = "CSS Code",
                    icon = Icons.Outlined.Code,
                    isSelected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.weight(1f).testTag("tab_css_code")
                )
                TabPill(
                    title = "Tokens (${currentTheme.name.split(" ")[0]})",
                    icon = Icons.Default.Tune,
                    isSelected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.weight(1f).testTag("tab_tokens")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTab == 0) {
                // CSS Code Block
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(CssTheme.vars.radiusSm))
                        .background(
                            if (CssTheme.vars.isDark) Color(0xFF000000) else Color(0xFFF6F7F9)
                        )
                        .border(
                            width = 1.dp,
                            color = CssTheme.vars.border,
                            shape = RoundedCornerShape(CssTheme.vars.radiusSm)
                        )
                        .padding(14.dp)
                ) {
                    Text(
                        text = currentTheme.toCssString(),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = if (CssTheme.vars.isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                        modifier = Modifier.testTag("css_snippet_text")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Copy Action Button
                Button(
                    onClick = {
                        context.copyToClipboard(currentTheme.toCssString(), label = "CSS Variables")
                        isCopied = true
                        Toast.makeText(context, "CSS Variables Copied to Clipboard!", Toast.LENGTH_SHORT).show()
                        scope.launch {
                            delay(2500)
                            isCopied = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("copy_css_btn"),
                    shape = RoundedCornerShape(CssTheme.vars.radiusSm),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CssTheme.vars.primary,
                        contentColor = CssTheme.vars.primaryForeground
                    )
                ) {
                    Icon(
                        imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = "Copy CSS",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isCopied) "CSS Copied!" else "Copy CSS Variables",
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }
            } else {
                // Token Swatches and Live Accent Tuning
                Text(
                    text = "CSS Color Tokens",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CssTheme.vars.cardForeground
                )
                Spacer(modifier = Modifier.height(8.dp))

                val tokens = listOf(
                    Triple("--bg", currentTheme.background, "Canvas background"),
                    Triple("--fg", currentTheme.foreground, "Primary text & elements"),
                    Triple("--card", currentTheme.card, "Surface / card elevation"),
                    Triple("--border", currentTheme.border, "Subtle structural border"),
                    Triple("--primary", currentTheme.primary, "Key focal / interactive accent"),
                    Triple("--muted", currentTheme.muted, "Subtle background container"),
                    Triple("--muted-fg", currentTheme.mutedForeground, "Secondary / label text"),
                    Triple("--accent", currentTheme.accent, "Interactive hover / secondary state"),
                    Triple("--ring", currentTheme.ring, "Focus ring & active indicator")
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tokens.forEach { (tokenName, color, desc) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(CssTheme.vars.radiusSm))
                                .background(CssTheme.vars.subtleSurface)
                                .border(1.dp, CssTheme.vars.border, RoundedCornerShape(CssTheme.vars.radiusSm))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(1.dp, CssTheme.vars.border, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tokenName,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CssTheme.vars.cardForeground
                                )
                                Text(
                                    text = desc,
                                    fontSize = 11.sp,
                                    color = CssTheme.vars.mutedForeground
                                )
                            }
                            Text(
                                text = "#${color.toHex()}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = CssTheme.vars.mutedForeground
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Live Accent Tuning
                Text(
                    text = "Live Override --primary Accent",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CssTheme.vars.cardForeground
                )
                Spacer(modifier = Modifier.height(8.dp))

                val customAccents = listOf(
                    Color(0xFF0070F3), // Vercel Blue
                    Color(0xFF5E6AD2), // Linear Indigo
                    Color(0xFFFF5500), // Braun Orange
                    Color(0xFF10B981), // Emerald
                    Color(0xFFEB5757), // Notion Terracotta
                    Color(0xFFF59E0B), // Amber
                    Color(0xFF8B5CF6), // Violet
                    Color(0xFFFAFAFA), // Pure White
                    Color(0xFF18181B)  // Obsidian Carbon
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    customAccents.forEach { accentColor ->
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(accentColor)
                                .border(
                                    width = if (currentTheme.primary == accentColor) 2.dp else 1.dp,
                                    color = if (currentTheme.primary == accentColor) CssTheme.vars.foreground else CssTheme.vars.border,
                                    shape = CircleShape
                                )
                                .clickable {
                                    onCustomPrimarySelected(accentColor)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (currentTheme.primary == accentColor) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = if (accentColor == Color(0xFFFAFAFA)) Color.Black else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun TabPill(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(CssTheme.vars.radiusSm))
            .background(if (isSelected) CssTheme.vars.card else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) CssTheme.vars.cardForeground else CssTheme.vars.mutedForeground,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) CssTheme.vars.cardForeground else CssTheme.vars.mutedForeground
            )
        }
    }
}
