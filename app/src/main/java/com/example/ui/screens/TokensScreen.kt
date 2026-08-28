package com.example.ui.screens

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CssVariables
import com.example.ui.theme.toHex

/**
 * TokensScreen: Live CSS Design Tokens Matrix and interactive UI sandbox adhering to W3C CSS tokens specs.
 */
@Composable
fun TokensScreen(
    currentTheme: CssVariables,
    onOpenInspector: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var interactiveCounter by remember { mutableStateOf(0) }

    val tokensList = listOf(
        Triple("--background", currentTheme.background, "Main screen surface canvas"),
        Triple("--foreground", currentTheme.foreground, "Primary high-contrast typography"),
        Triple("--primary", currentTheme.primary, "Brand accent & call-to-action color"),
        Triple("--primary-foreground", currentTheme.primaryForeground, "Contrast text inside primary buttons"),
        Triple("--card", currentTheme.card, "Elevated container surface"),
        Triple("--muted", currentTheme.muted, "Subtle background fill for secondary items"),
        Triple("--muted-foreground", currentTheme.mutedForeground, "Secondary editorial captions & labels"),
        Triple("--border", currentTheme.border, "1px structural hairline divider"),
        Triple("--ring", currentTheme.ring, "Focus and active halo indicator")
    ).filter {
        searchQuery.isEmpty() || it.first.contains(searchQuery, ignoreCase = true) || it.third.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(currentTheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .widthIn(max = 560.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Search & Filter Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Filter CSS variable tokens...", fontSize = 12.sp, color = currentTheme.mutedForeground) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search",
                    tint = currentTheme.mutedForeground,
                    modifier = Modifier.size(16.dp)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(currentTheme.radiusSm),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = currentTheme.card,
                unfocusedContainerColor = currentTheme.card,
                focusedBorderColor = currentTheme.primary,
                unfocusedBorderColor = currentTheme.border,
                focusedTextColor = currentTheme.foreground,
                unfocusedTextColor = currentTheme.foreground
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("tokens_search_input")
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Section 1: Live Component Token Playground
        Text(
            text = "LIVE TOKENIZED UI COMPONENTS",
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color = currentTheme.mutedForeground,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(currentTheme.radiusLg))
                .background(currentTheme.card)
                .border(1.dp, currentTheme.border, RoundedCornerShape(currentTheme.radiusLg))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Interactive Button Pair
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { interactiveCounter++ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = currentTheme.primary,
                            contentColor = currentTheme.primaryForeground
                        ),
                        shape = RoundedCornerShape(currentTheme.radiusSm),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "var(--primary) : $interactiveCounter",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(currentTheme.radiusSm))
                            .background(currentTheme.muted)
                            .border(1.dp, currentTheme.border, RoundedCornerShape(currentTheme.radiusSm))
                            .clickable { if (interactiveCounter > 0) interactiveCounter-- }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "var(--muted) : Decrement",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = currentTheme.foreground
                        )
                    }
                }

                // Interactive Badge & Chip Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(currentTheme.radiusLg))
                            .background(currentTheme.primary.copy(alpha = 0.12f))
                            .border(1.dp, currentTheme.primary.copy(alpha = 0.3f), RoundedCornerShape(currentTheme.radiusLg))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Badge Active",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = currentTheme.primary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(currentTheme.radiusLg))
                            .background(currentTheme.subtleSurface)
                            .border(1.dp, currentTheme.border, RoundedCornerShape(currentTheme.radiusLg))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Radius: ${currentTheme.radiusLg.value.toInt()}px",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.5.sp,
                            color = currentTheme.mutedForeground
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section 2: Tokens Matrix List
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ACTIVE CSS TOKENS (${tokensList.size})",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = currentTheme.mutedForeground
            )

            Text(
                text = "Tap hex to copy",
                fontSize = 10.sp,
                color = currentTheme.mutedForeground
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(currentTheme.radiusLg))
                .background(currentTheme.card)
                .border(1.dp, currentTheme.border, RoundedCornerShape(currentTheme.radiusLg))
        ) {
            tokensList.forEachIndexed { index, (prop, color, desc) ->
                val hexValue = "#" + color.toHex()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText(prop, hexValue))
                            Toast.makeText(context, "$prop ($hexValue) Copied", Toast.LENGTH_SHORT).show()
                        }
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Color Swatch
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(color)
                                .border(1.dp, currentTheme.border, RoundedCornerShape(4.dp))
                        )

                        Column {
                            Text(
                                text = prop,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = currentTheme.foreground
                            )
                            Text(
                                text = desc,
                                fontSize = 10.5.sp,
                                color = currentTheme.mutedForeground
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = hexValue,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = currentTheme.primary
                        )
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "Copy",
                            tint = currentTheme.mutedForeground,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                if (index < tokensList.size - 1) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp)
                            .height(0.5.dp)
                            .background(currentTheme.border.copy(alpha = 0.5f))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
