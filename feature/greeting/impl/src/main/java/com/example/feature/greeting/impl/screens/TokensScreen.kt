package com.example.feature.greeting.impl.screens

import com.example.core.ui.util.copyToClipboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.material3.Button as MaterialButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.ui.theme.CssVariables
import com.example.core.ui.theme.toHex
import com.example.feature.greeting.impl.R
import com.example.feature.greeting.impl.components.Button
import com.example.feature.greeting.impl.components.TokenSearchBar

private data class TokenRow(
    val prop: String,
    val color: Color,
    @StringRes val descRes: Int
)

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

    val tokenCopiedToastFmt = stringResource(R.string.tokens_copied_toast)

    val allTokens = listOf(
        TokenRow("--background", currentTheme.background, R.string.tokens_desc_background),
        TokenRow("--foreground", currentTheme.foreground, R.string.tokens_desc_foreground),
        TokenRow("--primary", currentTheme.primary, R.string.tokens_desc_primary),
        TokenRow("--primary-foreground", currentTheme.primaryForeground, R.string.tokens_desc_primary_foreground),
        TokenRow("--card", currentTheme.card, R.string.tokens_desc_card),
        TokenRow("--muted", currentTheme.muted, R.string.tokens_desc_muted),
        TokenRow("--muted-foreground", currentTheme.mutedForeground, R.string.tokens_desc_muted_foreground),
        TokenRow("--border", currentTheme.border, R.string.tokens_desc_border),
        TokenRow("--ring", currentTheme.ring, R.string.tokens_desc_ring)
    )
    val tokensList = allTokens
        .map { row -> row to stringResource(row.descRes) }
        .filter { (row, desc) ->
            searchQuery.isEmpty() || row.prop.contains(searchQuery, ignoreCase = true) || desc.contains(searchQuery, ignoreCase = true)
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
        // Search & Filter Bar (extracted component)
        TokenSearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            currentTheme = currentTheme
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Section 1: Live Component Token Playground
        Text(
            text = stringResource(R.string.tokens_live_components),
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
                    MaterialButton(
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

                    Button(
                        onClick = { if (interactiveCounter > 0) interactiveCounter-- },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(currentTheme.radiusSm)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(currentTheme.muted)
                                .border(1.dp, currentTheme.border, RoundedCornerShape(currentTheme.radiusSm))
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
                            text = stringResource(R.string.tokens_badge_active),
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
                            text = stringResource(R.string.tokens_radius) + ": ${currentTheme.radiusLg.value.toInt()}px",
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
                text = stringResource(R.string.tokens_active_css_tokens, tokensList.size),
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = currentTheme.mutedForeground
            )

            Text(
                text = stringResource(R.string.tokens_tap_hex_copy),
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
            tokensList.forEachIndexed { index, (row, desc) ->
                val hexValue = "#" + row.color.toHex()
                Button(
                    onClick = {
                        context.copyToClipboard(hexValue, label = row.prop)
                        Toast.makeText(context, tokenCopiedToastFmt.format(row.prop, hexValue), Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
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
                                .background(row.color)
                                .border(1.dp, currentTheme.border, RoundedCornerShape(4.dp))
                        )

                        Column {
                            Text(
                                text = row.prop,
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
                            contentDescription = stringResource(R.string.tokens_cd_copy),
                            tint = currentTheme.mutedForeground,
                            modifier = Modifier.size(12.dp)
                        )
                    }
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
