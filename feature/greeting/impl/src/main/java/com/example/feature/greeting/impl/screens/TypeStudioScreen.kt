package com.example.feature.greeting.impl.screens

import com.example.core.ui.util.copyToClipboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.ui.theme.CssVariables

/**
 * TypeStudioScreen: An editorial-grade Typography Playground inspired by Apple Typography guidelines,
 * Vercel Geist Font Lab, and Google Fonts spec.
 */
@Composable
fun TypeStudioScreen(
    currentTheme: CssVariables,
    selectedTypography: AppTypographyChoice,
    onTypographyChange: (AppTypographyChoice) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var fontSizeSlider by remember { mutableFloatStateOf(42f) }
    var letterSpacingSlider by remember { mutableFloatStateOf(-0.5f) }
    var isItalic by remember { mutableStateOf(false) }

    val samplePhrases = listOf(
        "Form follows function.",
        "Simplicity is the ultimate sophistication.",
        "Good design is as little design as possible.",
        "The details are not the details. They make the design."
    )
    var selectedPhraseIndex by remember { mutableStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(currentTheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .widthIn(max = 560.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Section 1: Typography Family Switcher
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TYPE ENGINE",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = currentTheme.mutedForeground
            )

            // Italic Toggle Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(currentTheme.radiusSm))
                    .background(if (isItalic) currentTheme.subtleSurface else Color.Transparent)
                    .border(1.dp, if (isItalic) currentTheme.primary else currentTheme.border, RoundedCornerShape(currentTheme.radiusSm))
                    .clickable { isItalic = !isItalic }
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "Italic",
                    fontStyle = FontStyle.Italic,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isItalic) currentTheme.primary else currentTheme.mutedForeground
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3 Font Family Options
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppTypographyChoice.values().forEach { choice ->
                val isSelected = selectedTypography == choice
                val bg by animateColorAsState(
                    targetValue = if (isSelected) currentTheme.card else currentTheme.subtleSurface.copy(alpha = 0.5f),
                    label = "font_choice_bg"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(currentTheme.radiusSm))
                        .background(bg)
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) currentTheme.primary else currentTheme.border,
                            shape = RoundedCornerShape(currentTheme.radiusSm)
                        )
                        .clickable { onTypographyChange(choice) }
                        .padding(vertical = 10.dp, horizontal = 6.dp)
                        .testTag("type_studio_choice_${choice.name}"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Ag",
                            fontFamily = choice.font,
                            fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) currentTheme.primary else currentTheme.foreground
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = when (choice) {
                                AppTypographyChoice.EDITORIAL -> "Serif"
                                AppTypographyChoice.SANS -> "Sans"
                                AppTypographyChoice.MONO -> "Mono"
                            },
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) currentTheme.foreground else currentTheme.mutedForeground
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section 2: Interactive Specimen Hero Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(currentTheme.radiusLg))
                .background(currentTheme.card)
                .border(1.dp, currentTheme.border, RoundedCornerShape(currentTheme.radiusLg))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header badge inside canvas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedTypography.title.uppercase(),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = currentTheme.mutedForeground
                    )

                    IconButton(
                        onClick = {
                            val textToCopy = samplePhrases[selectedPhraseIndex]
                            context.copyToClipboard(textToCopy, label = "Typography Sample")
                            Toast.makeText(context, "Specimen Copied", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "Copy text",
                            tint = currentTheme.mutedForeground,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // The dynamic live rendered typography
                Text(
                    text = samplePhrases[selectedPhraseIndex],
                    fontFamily = selectedTypography.font,
                    fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
                    fontSize = fontSizeSlider.sp,
                    letterSpacing = letterSpacingSlider.sp,
                    lineHeight = (fontSizeSlider * 1.15f).sp,
                    fontWeight = FontWeight.Normal,
                    color = currentTheme.foreground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Phrase Switcher Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    samplePhrases.forEachIndexed { idx, _ ->
                        val isPhraseSelected = idx == selectedPhraseIndex
                        Box(
                            modifier = Modifier
                                .size(if (isPhraseSelected) 18.dp else 8.dp, 8.dp)
                                .clip(CircleShape)
                                .background(if (isPhraseSelected) currentTheme.primary else currentTheme.border)
                                .clickable { selectedPhraseIndex = idx }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section 3: Fine-Tuning Sliders (Size & Tracking)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(currentTheme.radiusLg))
                .background(currentTheme.card)
                .border(1.dp, currentTheme.border, RoundedCornerShape(currentTheme.radiusLg))
                .padding(16.dp)
        ) {
            Column {
                // Size Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Font Size",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = currentTheme.foreground
                    )
                    Text(
                        text = "${fontSizeSlider.toInt()} sp",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = currentTheme.primary
                    )
                }

                Slider(
                    value = fontSizeSlider,
                    onValueChange = { fontSizeSlider = it },
                    valueRange = 24f..64f,
                    colors = SliderDefaults.colors(
                        thumbColor = currentTheme.primary,
                        activeTrackColor = currentTheme.primary,
                        inactiveTrackColor = currentTheme.border
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Letter Spacing Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Letter Spacing (Tracking)",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = currentTheme.foreground
                    )
                    Text(
                        text = "%.1f sp".format(letterSpacingSlider),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = currentTheme.primary
                    )
                }

                Slider(
                    value = letterSpacingSlider,
                    onValueChange = { letterSpacingSlider = it },
                    valueRange = -2.0f..4.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = currentTheme.primary,
                        activeTrackColor = currentTheme.primary,
                        inactiveTrackColor = currentTheme.border
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section 4: Glyph Matrix Specimen
        Text(
            text = "GLYPH SPECIMEN",
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
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "A B C D E F G H I J K L M N O P Q R S T U V W X Y Z",
                    fontFamily = selectedTypography.font,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 2.sp,
                    color = currentTheme.foreground
                )
                Text(
                    text = "a b c d e f g h i j k l m n o p q r s t u v w x y z",
                    fontFamily = selectedTypography.font,
                    fontSize = 13.sp,
                    color = currentTheme.mutedForeground,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "0 1 2 3 4 5 6 7 8 9 · ! @ # $ % ^ & * ( ) _ +",
                    fontFamily = selectedTypography.font,
                    fontSize = 13.sp,
                    color = currentTheme.mutedForeground,
                    letterSpacing = 2.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
