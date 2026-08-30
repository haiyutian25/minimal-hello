package com.example.feature.greeting.impl.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.ui.theme.CssVariables
import com.example.core.ui.theme.isBraun

/**
 * One entry of a [SegmentedControl]. [font] optionally renders the label in a
 * specific typeface (used by the typography switcher); null uses the default.
 */
data class SegmentedOption(
    val id: String,
    val label: String,
    val font: FontFamily? = null,
)

/**
 * Generic equal-width segmented selector (a row of mutually exclusive pills).
 *
 * The selected pill gets a clearly elevated surface — pure white in light mode,
 * a lighter muted surface in dark mode — so the active choice reads instantly
 * against the gray [CssVariables.card] track (previously it stayed gray and the
 * selection was barely visible).
 */
@Composable
fun SegmentedControl(
    options: List<SegmentedOption>,
    selectedId: String,
    onSelect: (String) -> Unit,
    currentTheme: CssVariables,
    modifier: Modifier = Modifier,
) {
    // Elevated selected surface (white in light / muted in dark); Braun (Dieter
    // Rams) keeps its original restrained subtleSurface instead.
    val selectedPillBackground = when {
        currentTheme.isBraun -> currentTheme.subtleSurface
        currentTheme.isDark -> currentTheme.muted
        else -> Color.White
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(currentTheme.radiusMd))
            .background(currentTheme.card)
            .border(1.dp, currentTheme.border.copy(alpha = 0.5f), RoundedCornerShape(currentTheme.radiusMd))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { option ->
            val isSelected = option.id == selectedId

            Button(
                onClick = { onSelect(option.id) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(currentTheme.radiusSm)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isSelected) selectedPillBackground else Color.Transparent)
                        .border(
                            width = if (isSelected) 1.dp else 0.dp,
                            color = if (isSelected) currentTheme.border else Color.Transparent,
                            shape = RoundedCornerShape(currentTheme.radiusSm)
                        )
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option.label,
                        fontFamily = option.font ?: FontFamily.Default,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) currentTheme.foreground else currentTheme.mutedForeground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
