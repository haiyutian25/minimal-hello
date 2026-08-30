package com.example.feature.greeting.impl.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.ui.theme.CssVariables
import com.example.feature.greeting.impl.R
import com.example.feature.greeting.impl.components.Button
import kotlin.math.roundToInt

/**
 * App-wide typography choice. Persisted by name; each entry carries its
 * display strings and the [FontFamily] it maps to.
 */
enum class AppTypographyChoice(@StringRes val titleRes: Int, @StringRes val subtitleRes: Int, val font: FontFamily) {
    EDITORIAL(R.string.settings_typography_editorial_title, R.string.settings_typography_editorial_subtitle, FontFamily.Serif),
    SANS(R.string.settings_typography_sans_title, R.string.settings_typography_sans_subtitle, FontFamily.SansSerif),
    MONO(R.string.settings_typography_mono_title, R.string.settings_typography_mono_subtitle, FontFamily.Monospace)
}

/**
 * Dedicated font settings page: the typography engine (font family) picker and
 * an entry that opens the font-size adjustment page.
 */
@Composable
fun FontScreen(
    currentTheme: CssVariables,
    selectedTypography: AppTypographyChoice,
    onTypographyChange: (AppTypographyChoice) -> Unit,
    fontScale: Float,
    onOpenFontSize: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(currentTheme.background)
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp)
            .widthIn(max = 560.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ==========================================
        // SECTION 1: Typography engine (font family)
        // ==========================================
        FontSectionHeader(
            title = stringResource(R.string.settings_section_typography),
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

                Button(
                    onClick = { onTypographyChange(style) },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "settings_typography_${style.name}"
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
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
                                    text = stringResource(style.titleRes),
                                    fontFamily = style.font,
                                    fontSize = 13.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = currentTheme.foreground
                                )
                                Text(
                                    text = stringResource(style.subtitleRes),
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
        // SECTION 2: Font size -> dedicated adjust page
        // ==========================================
        FontSectionHeader(
            title = stringResource(R.string.settings_section_font_size),
            icon = Icons.Outlined.TextFields,
            currentTheme = currentTheme
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { onOpenFontSize() },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(currentTheme.radiusMd),
            testTag = "font_size_entry"
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(currentTheme.card)
                    .border(1.dp, currentTheme.border, RoundedCornerShape(currentTheme.radiusMd))
                    .padding(horizontal = 14.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.settings_font_size_label),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = currentTheme.foreground
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.settings_font_size_entry_subtitle),
                        fontSize = 11.sp,
                        color = currentTheme.mutedForeground
                    )
                }

                Text(
                    text = "${(fontScale * 100).roundToInt()}%",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = currentTheme.primary
                )

                Spacer(modifier = Modifier.width(8.dp))

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

@Composable
private fun FontSectionHeader(
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
