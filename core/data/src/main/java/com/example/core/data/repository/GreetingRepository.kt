package com.example.core.data.repository

import javax.inject.Inject

/**
 * One curated typographic statement shown on the hero card.
 * [part1] renders as the light italic line, [part2] as the bold line.
 */
data class HeroQuote(val part1: String, val part2: String)

/**
 * Content source for the greeting feature. Backed by static curation today;
 * can be swapped for a remote implementation without touching the ViewModel.
 */
interface GreetingRepository {
    val heroQuotes: List<HeroQuote>
    val heroCaptions: List<String>
}

class GreetingRepositoryImpl @Inject constructor() : GreetingRepository {

    override val heroQuotes: List<HeroQuote> = listOf(
        HeroQuote("Hello", "World."),
        HeroQuote("Less,", "Better."),
        HeroQuote("Pure", "Form."),
        HeroQuote("Spatial", "Balance."),
        HeroQuote("Calm", "Clarity."),
        HeroQuote("Quiet", "Precision."),
    )

    override val heroCaptions: List<String> = listOf(
        "A production-grade minimalist interface focused on typographic hierarchy, subtle micro-interactions, and spatial balance.",
        "Form follows intention. Calibrated negative space and micro-contrast let meaning resonate naturally.",
        "Achromatic foundations paired with surgical accents produce distraction-free aesthetic clarity.",
        "Design tokens structured directly through web-standard CSS custom variables and 4px grid rules.",
        "Restrained proportions and physics-based spring transitions offer frictionless feedback.",
    )
}
