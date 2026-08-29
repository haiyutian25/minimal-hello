package com.example.feature.greeting.impl.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.core.ui.theme.CssVariables

/**
 * Selectable card button: a themed card container whose border highlights when
 * selected (primary color + thicker). Used for mode / font-family pickers.
 *
 * This is a dedicated card-style control, independent from the generic
 * behavior-only [Button]: it owns the card chrome (background + selection-aware
 * border + padding) while the caller still provides the inner [content].
 *
 * @param isSelected          drives the border highlight
 * @param shape               card corner shape
 * @param containerColor      card background (may be an animated color)
 * @param selectedBorderWidth border width when selected (1.dp when not)
 * @param contentPadding      inner padding around [content]
 * @param contentAlignment    alignment of [content] inside the card
 */
@Composable
fun CardButton(
    onClick: () -> Unit,
    isSelected: Boolean,
    currentTheme: CssVariables,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(currentTheme.radiusMd),
    containerColor: Color = currentTheme.card,
    selectedBorderWidth: Dp = 2.dp,
    contentPadding: PaddingValues = PaddingValues(14.dp),
    contentAlignment: Alignment = Alignment.TopStart,
    testTag: String? = null,
    content: @Composable () -> Unit
) {
    val tagged = if (testTag != null) modifier.testTag(testTag) else modifier

    Box(
        modifier = tagged
            .clip(shape)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(containerColor)
                .border(
                    width = if (isSelected) selectedBorderWidth else 1.dp,
                    color = if (isSelected) currentTheme.primary else currentTheme.border,
                    shape = shape
                )
                .padding(contentPadding),
            contentAlignment = contentAlignment
        ) {
            content()
        }
    }
}
