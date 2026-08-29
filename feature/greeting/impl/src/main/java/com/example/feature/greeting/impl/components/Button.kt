package com.example.feature.greeting.impl.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag

/**
 * Shared behavior-only button.
 *
 * The component is responsible for interaction only (click handling, ripple
 * control, clipping for press feedback, test tag). It does NOT own any
 * appearance: each call site provides its own visual content via [content],
 * so every usage keeps its exact look while sharing one button abstraction.
 *
 * Note: this is the feature's own button, distinct from Material 3's
 * `androidx.compose.material3.Button` (import with an alias where both meet).
 *
 * @param shape         clip shape so the press ripple follows rounded corners
 * @param rippleEnabled set false to suppress press feedback entirely
 * @param content       the caller-provided visual (background, border, text...)
 */
@Composable
fun Button(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    rippleEnabled: Boolean = true,
    testTag: String? = null,
    content: @Composable () -> Unit
) {
    val tagged = if (testTag != null) modifier.testTag(testTag) else modifier
    val clickModifier = if (rippleEnabled) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    }

    Box(
        modifier = tagged
            .clip(shape)
            .then(clickModifier)
    ) {
        content()
    }
}
