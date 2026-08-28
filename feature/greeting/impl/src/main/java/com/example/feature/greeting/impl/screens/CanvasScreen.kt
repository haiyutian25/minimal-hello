package com.example.feature.greeting.impl.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.ui.theme.CssTheme
import com.example.core.ui.theme.CssVariables
import com.example.core.ui.theme.ProductionPalettes
import com.example.core.ui.theme.toHex
import com.example.core.ui.util.copyToClipboard
import com.example.feature.greeting.impl.GreetingViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class TypographyStyle(val label: String, val font: FontFamily) {
    EDITORIAL("Serif", FontFamily.Serif),
    SANS("Sans", FontFamily.SansSerif),
    MONO("Mono", FontFamily.Monospace)
}

/**
 * Craft canvas tab: telemetry header, preset switcher, hero greeting card,
 * custom greeting editor, typography engine selector and live CSS tokens.
 */
@Composable
fun CanvasScreen(
    viewModel: GreetingViewModel,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    val currentTheme by viewModel.currentTheme.collectAsState()
    val typographyChoice by viewModel.typographyChoice.collectAsState()
    val greetingIndex by viewModel.greetingIndex.collectAsState()
    val customGreeting by viewModel.customGreeting.collectAsState()

    val selectedTypography = when (typographyChoice) {
        AppTypographyChoice.EDITORIAL -> TypographyStyle.EDITORIAL
        AppTypographyChoice.SANS -> TypographyStyle.SANS
        AppTypographyChoice.MONO -> TypographyStyle.MONO
    }

    var showCustomGreetingInput by remember { mutableStateOf(false) }
    var customPart1 by remember { mutableStateOf(customGreeting.part1) }
    var customPart2 by remember { mutableStateOf(customGreeting.part2) }
    var isGreetingPressed by remember { mutableStateOf(false) }
    var isCopied by remember { mutableStateOf(false) }

    // Minimal Telemetry: Frame latency simulator / Craft metrics
    var renderLatencyMs by remember { mutableLongStateOf(4) }
    LaunchedEffect(currentTheme) {
        renderLatencyMs = (3..5).random().toLong()
    }

    // Palette family identifier
    val currentPresetBase = when {
        currentTheme.themeId.startsWith("editorial") -> "Editorial"
        currentTheme.themeId.startsWith("geist") -> "Geist"
        currentTheme.themeId.startsWith("linear") -> "Linear"
        currentTheme.themeId.startsWith("shadcn") -> "Shadcn"
        currentTheme.themeId.startsWith("notion") -> "Notion"
        else -> "Braun"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .widthIn(max = 560.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Subtle Craft Metric / Telemetry Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "DESIGN TOKENS",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = currentTheme.mutedForeground,
                    modifier = Modifier.testTag("editorial_version_label")
                )

                // Minimal Latency pill (Linear / Vercel pattern)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(currentTheme.radiusSm))
                        .background(currentTheme.subtleSurface)
                        .border(0.5.dp, currentTheme.border.copy(alpha = 0.5f), RoundedCornerShape(currentTheme.radiusSm))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${renderLatencyMs}ms",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = currentTheme.mutedForeground
                    )
                }
            }

            // Quick CSS Inspector pill trigger
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(currentTheme.radiusSm))
                    .background(currentTheme.subtleSurface)
                    .border(1.dp, currentTheme.border.copy(alpha = 0.5f), RoundedCornerShape(currentTheme.radiusSm))
                    .clickable { viewModel.showInspector() }
                    .padding(horizontal = 8.dp, vertical = 3.5.dp)
                    .testTag("open_inspector_quick_pill")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Tune,
                    contentDescription = null,
                    tint = currentTheme.primary,
                    modifier = Modifier.size(11.dp)
                )
                Text(
                    text = "Inspect CSS",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = currentTheme.foreground
                )
            }
        }

        // 2. Linear-Style Preset Palette Horizontal Micro-Switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val presets = listOf(
                Triple("Editorial", ProductionPalettes.EditorialLight, ProductionPalettes.EditorialDark),
                Triple("Geist", ProductionPalettes.GeistLight, ProductionPalettes.GeistDark),
                Triple("Linear", ProductionPalettes.LinearLight, ProductionPalettes.LinearDark),
                Triple("Shadcn", ProductionPalettes.ShadcnZincLight, ProductionPalettes.ShadcnZincDark),
                Triple("Notion", ProductionPalettes.NotionWarmLight, ProductionPalettes.NotionWarmDark),
                Triple("Braun", ProductionPalettes.DieterRamsLight, ProductionPalettes.DieterRamsDark)
            )

            presets.forEach { (name, lightVariant, darkVariant) ->
                val isSelected = currentPresetBase == name
                val targetVariant = if (currentTheme.isDark) darkVariant else lightVariant

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(currentTheme.radiusLg))
                        .background(if (isSelected) currentTheme.card else currentTheme.subtleSurface)
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) currentTheme.primary else currentTheme.border.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(currentTheme.radiusLg)
                        )
                        .clickable {
                            viewModel.selectTheme(targetVariant)
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("preset_pill_$name")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(targetVariant.primary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = name,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) currentTheme.foreground else currentTheme.mutedForeground,
                            letterSpacing = 0.2.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. Central Hero Canvas: Pure Typographic Expression & Minimalist Entrance Animation
        var entranceTrigger by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            entranceTrigger = true
        }

        val luxuryEasing = remember { CubicBezierEasing(0.16f, 1f, 0.3f, 1f) }

        // Staggered Entrance Animations
        val cardEntranceAlpha by animateFloatAsState(
            targetValue = if (entranceTrigger) 1f else 0f,
            animationSpec = tween(durationMillis = 650, delayMillis = 60, easing = luxuryEasing),
            label = "card_entrance_alpha"
        )
        val cardEntranceOffsetY by animateDpAsState(
            targetValue = if (entranceTrigger) 0.dp else 16.dp,
            animationSpec = tween(durationMillis = 650, delayMillis = 60, easing = luxuryEasing),
            label = "card_entrance_offset"
        )
        val eyebrowEntranceAlpha by animateFloatAsState(
            targetValue = if (entranceTrigger) 1f else 0f,
            animationSpec = tween(durationMillis = 500, delayMillis = 120, easing = luxuryEasing),
            label = "eyebrow_entrance_alpha"
        )
        val text1EntranceAlpha by animateFloatAsState(
            targetValue = if (entranceTrigger) 1f else 0f,
            animationSpec = tween(durationMillis = 700, delayMillis = 180, easing = luxuryEasing),
            label = "text1_entrance_alpha"
        )
        val text1EntranceOffsetY by animateDpAsState(
            targetValue = if (entranceTrigger) 0.dp else 18.dp,
            animationSpec = tween(durationMillis = 700, delayMillis = 180, easing = luxuryEasing),
            label = "text1_entrance_offset"
        )
        val text2EntranceAlpha by animateFloatAsState(
            targetValue = if (entranceTrigger) 1f else 0f,
            animationSpec = tween(durationMillis = 750, delayMillis = 300, easing = luxuryEasing),
            label = "text2_entrance_alpha"
        )
        val text2EntranceOffsetY by animateDpAsState(
            targetValue = if (entranceTrigger) 0.dp else 22.dp,
            animationSpec = tween(durationMillis = 750, delayMillis = 300, easing = luxuryEasing),
            label = "text2_entrance_offset"
        )
        val ruleEntranceWidth by animateDpAsState(
            targetValue = if (entranceTrigger) 48.dp else 0.dp,
            animationSpec = tween(durationMillis = 600, delayMillis = 420, easing = luxuryEasing),
            label = "rule_entrance_width"
        )
        val captionEntranceAlpha by animateFloatAsState(
            targetValue = if (entranceTrigger) 1f else 0f,
            animationSpec = tween(durationMillis = 600, delayMillis = 480, easing = luxuryEasing),
            label = "caption_entrance_alpha"
        )
        val captionEntranceOffsetY by animateDpAsState(
            targetValue = if (entranceTrigger) 0.dp else 8.dp,
            animationSpec = tween(durationMillis = 600, delayMillis = 480, easing = luxuryEasing),
            label = "caption_entrance_offset"
        )

        val heroQuotes = viewModel.heroQuotes
        val heroCaptions = viewModel.heroCaptions
        val activeHeading = if (customGreeting.isActive) {
            Pair(customGreeting.part1, customGreeting.part2)
        } else {
            val quote = heroQuotes[greetingIndex % heroQuotes.size]
            Pair(quote.part1, quote.part2)
        }
        val activeCaption = heroCaptions[greetingIndex % heroCaptions.size]

        val heroScale by animateFloatAsState(
            targetValue = if (isGreetingPressed) 0.985f else 1.0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "hero_spring"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = cardEntranceOffsetY)
                .alpha(cardEntranceAlpha)
                .scale(heroScale)
                .clip(RoundedCornerShape(currentTheme.radiusLg))
                .background(currentTheme.card)
                .border(
                    width = 1.dp,
                    color = currentTheme.border,
                    shape = RoundedCornerShape(currentTheme.radiusLg)
                )
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isGreetingPressed = true
                            tryAwaitRelease()
                            isGreetingPressed = false
                        },
                        onTap = {
                            viewModel.nextGreeting()
                        }
                    )
                }
                .padding(horizontal = 26.dp, vertical = 34.dp)
                .testTag("hello_world_hero_card")
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                // Eyebrow Label & Interactive Quick-Cycle badge
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(eyebrowEntranceAlpha),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SAMPLE APPLICATION",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 3.sp,
                        color = currentTheme.mutedForeground
                    )

                    Text(
                        text = "${(greetingIndex % heroQuotes.size) + 1} / ${heroQuotes.size}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = currentTheme.mutedForeground.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Hero Animated Headline: High-Craft Typography Pairing with Staggered Minimalist Entrance
                AnimatedContent(
                    targetState = activeHeading,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(240, delayMillis = 30)) +
                            slideInVertically(animationSpec = tween(240)) { it / 6 })
                            .togetherWith(
                                fadeOut(animationSpec = tween(160)) +
                                    slideOutVertically(animationSpec = tween(160)) { -it / 6 }
                            )
                    },
                    label = "editorial_headline_anim"
                ) { (part1, part2) ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = part1,
                            fontFamily = when (selectedTypography) {
                                TypographyStyle.EDITORIAL -> FontFamily.Serif
                                TypographyStyle.SANS -> FontFamily.SansSerif
                                TypographyStyle.MONO -> FontFamily.Monospace
                            },
                            fontStyle = if (selectedTypography == TypographyStyle.EDITORIAL) FontStyle.Italic else FontStyle.Normal,
                            fontSize = 66.sp,
                            fontWeight = FontWeight.Light,
                            color = currentTheme.foreground,
                            letterSpacing = (-2.8).sp,
                            lineHeight = 64.sp,
                            modifier = Modifier
                                .offset(y = text1EntranceOffsetY)
                                .alpha(text1EntranceAlpha)
                                .testTag("editorial_heading_part1")
                        )
                        Text(
                            text = part2,
                            fontFamily = when (selectedTypography) {
                                TypographyStyle.EDITORIAL -> FontFamily.SansSerif
                                TypographyStyle.SANS -> FontFamily.SansSerif
                                TypographyStyle.MONO -> FontFamily.Monospace
                            },
                            fontSize = 66.sp,
                            fontWeight = FontWeight.Bold,
                            color = currentTheme.foreground,
                            letterSpacing = (-2.8).sp,
                            lineHeight = 58.sp,
                            modifier = Modifier
                                .padding(start = if (selectedTypography == TypographyStyle.EDITORIAL) 18.dp else 0.dp)
                                .offset(y = text2EntranceOffsetY)
                                .alpha(text2EntranceAlpha)
                                .testTag("editorial_heading_part2")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Editorial Accent Rule (h-[1px] w-12 bg-[var(--accent)]) with Smooth Width Expansion
                Box(
                    modifier = Modifier
                        .width(ruleEntranceWidth)
                        .height(1.5.dp)
                        .background(currentTheme.primary)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Editorial Caption with Staggered Fade & Translation
                AnimatedContent(
                    targetState = activeCaption,
                    transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
                    label = "caption_anim"
                ) { caption ->
                    Text(
                        text = caption,
                        fontSize = 13.5.sp,
                        lineHeight = 20.sp,
                        color = currentTheme.mutedForeground,
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .offset(y = captionEntranceOffsetY)
                            .alpha(captionEntranceAlpha)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Tactile micro-instruction chip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(currentTheme.radiusSm))
                            .background(currentTheme.subtleSurface)
                            .border(1.dp, currentTheme.border.copy(alpha = 0.4f), RoundedCornerShape(currentTheme.radiusSm))
                            .padding(horizontal = 9.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = currentTheme.mutedForeground,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Tap canvas to cycle statements",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.5.sp,
                            color = currentTheme.mutedForeground
                        )
                    }

                    // Custom text input toggle
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(currentTheme.radiusSm))
                            .background(if (showCustomGreetingInput) currentTheme.primary.copy(alpha = 0.1f) else Color.Transparent)
                            .clickable { showCustomGreetingInput = !showCustomGreetingInput }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (showCustomGreetingInput) "Close edit" else "Custom text",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = currentTheme.primary
                        )
                    }
                }
            }
        }

        // Inline Custom Text Editor (Advanced Minimalist Pattern)
        AnimatedVisibility(
            visible = showCustomGreetingInput,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .clip(RoundedCornerShape(currentTheme.radiusMd))
                    .background(currentTheme.card)
                    .border(1.dp, currentTheme.border, RoundedCornerShape(currentTheme.radiusMd))
                    .padding(14.dp)
            ) {
                Text(
                    text = "CUSTOM GREETING",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.sp,
                    color = currentTheme.mutedForeground,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Part 1 input (Italic serif / prefix)
                    BasicTextField(
                        value = customPart1,
                        onValueChange = {
                            customPart1 = it
                            viewModel.updateCustomGreeting(customPart1, customPart2)
                        },
                        textStyle = TextStyle(
                            color = currentTheme.foreground,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Serif,
                            fontStyle = FontStyle.Italic
                        ),
                        cursorBrush = SolidColor(currentTheme.primary),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(currentTheme.radiusSm))
                            .background(currentTheme.subtleSurface)
                            .border(1.dp, currentTheme.border, RoundedCornerShape(currentTheme.radiusSm))
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    )

                    // Part 2 input (Bold sans / suffix)
                    BasicTextField(
                        value = customPart2,
                        onValueChange = {
                            customPart2 = it
                            viewModel.updateCustomGreeting(customPart1, customPart2)
                        },
                        textStyle = TextStyle(
                            color = currentTheme.foreground,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                        cursorBrush = SolidColor(currentTheme.primary),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(currentTheme.radiusSm))
                            .background(currentTheme.subtleSurface)
                            .border(1.dp, currentTheme.border, RoundedCornerShape(currentTheme.radiusSm))
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Typography Engine Selector (Editorial Serif, Sans Modern, Mono Code)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(currentTheme.radiusMd))
                .background(currentTheme.card)
                .border(1.dp, currentTheme.border.copy(alpha = 0.5f), RoundedCornerShape(currentTheme.radiusMd))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TypographyStyle.entries.forEach { style ->
                val isSelected = selectedTypography == style
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(currentTheme.radiusSm))
                        .background(if (isSelected) currentTheme.subtleSurface else Color.Transparent)
                        .border(
                            width = if (isSelected) 1.dp else 0.dp,
                            color = if (isSelected) currentTheme.border else Color.Transparent,
                            shape = RoundedCornerShape(currentTheme.radiusSm)
                        )
                        .clickable {
                            viewModel.selectTypography(
                                when (style) {
                                    TypographyStyle.EDITORIAL -> AppTypographyChoice.EDITORIAL
                                    TypographyStyle.SANS -> AppTypographyChoice.SANS
                                    TypographyStyle.MONO -> AppTypographyChoice.MONO
                                }
                            )
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = style.label,
                        fontFamily = style.font,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) currentTheme.foreground else currentTheme.mutedForeground
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live CSS Tokens & Code Inspector Snippet
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(currentTheme.radiusLg))
                .background(currentTheme.card)
                .border(1.dp, currentTheme.border, RoundedCornerShape(currentTheme.radiusLg))
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Code,
                        contentDescription = null,
                        tint = currentTheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CSS Variables",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = currentTheme.foreground,
                        letterSpacing = (-0.2).sp
                    )
                }

                // Copy snippet button with instant visual confirmation
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(currentTheme.radiusLg))
                        .background(currentTheme.subtleSurface)
                        .border(1.dp, currentTheme.border, RoundedCornerShape(currentTheme.radiusLg))
                        .clickable {
                            context.copyToClipboard(currentTheme.toCssString(), label = "CSS Variables")
                            isCopied = true
                            Toast.makeText(context, "CSS Variables Copied", Toast.LENGTH_SHORT).show()
                            scope.launch {
                                delay(1800)
                                isCopied = false
                            }
                        }
                        .padding(horizontal = 9.dp, vertical = 4.5.dp)
                        .testTag("quick_copy_css_btn"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = if (isCopied) currentTheme.primary else currentTheme.mutedForeground,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isCopied) "Copied" else "Copy CSS",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isCopied) currentTheme.primary else currentTheme.mutedForeground
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Standard CSS Root Token List
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(currentTheme.radiusMd))
                    .background(
                        if (currentTheme.isDark) Color(0xFF070707) else Color(0xFFF7F7F7)
                    )
                    .border(1.dp, currentTheme.border.copy(alpha = 0.5f), RoundedCornerShape(currentTheme.radiusMd))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    CssVarLine(name = "--bg", value = "#${currentTheme.background.toHex()}", color = currentTheme.background)
                    CssVarLine(name = "--text", value = "#${currentTheme.foreground.toHex()}", color = currentTheme.foreground)
                    CssVarLine(name = "--surface", value = "#${currentTheme.card.toHex()}", color = currentTheme.card)
                    CssVarLine(name = "--accent", value = "#${currentTheme.primary.toHex()}", color = currentTheme.primary)
                    CssVarLine(name = "--muted", value = "#${currentTheme.mutedForeground.toHex()}", color = currentTheme.mutedForeground)
                    CssVarLine(name = "--border", value = "#${currentTheme.border.toHex()}", color = currentTheme.border)
                    CssVarLine(name = "--radius", value = "${currentTheme.radiusLg.value.toInt()}px", color = currentTheme.primary)
                }
            }
        }
    }
}

@Composable
private fun CssVarLine(
    name: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = name,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = CssTheme.vars.foreground
            )
            Text(
                text = ":",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = CssTheme.vars.mutedForeground
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = value,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = CssTheme.vars.mutedForeground
            )
        }

        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
                .border(1.dp, CssTheme.vars.border, RoundedCornerShape(2.dp))
        )
    }
}
