package com.example.core.data.repository

import androidx.annotation.StringRes
import com.example.core.data.R
import javax.inject.Inject

/**
 * One curated typographic statement shown on the hero card.
 * [part1Res] renders as the light italic line, [part2Res] as the bold line.
 * Holds string resource IDs (not resolved strings) so the UI resolves them
 * against the active locale — in-app language switches apply instantly,
 * without a process restart.
 */
data class HeroQuote(@StringRes val part1Res: Int, @StringRes val part2Res: Int)

/**
 * Content source for the greeting feature. Backed by static curation today;
 * can be swapped for a remote implementation without touching the ViewModel.
 * Exposes string resource IDs so the UI layer resolves them with the locale.
 */
interface GreetingRepository {
    val heroQuotes: List<HeroQuote>
    val heroCaptions: List<Int>
}

class GreetingRepositoryImpl @Inject constructor() : GreetingRepository {

    override val heroQuotes: List<HeroQuote> = listOf(
        HeroQuote(R.string.hero_quote_1_part1, R.string.hero_quote_1_part2),
        HeroQuote(R.string.hero_quote_2_part1, R.string.hero_quote_2_part2),
        HeroQuote(R.string.hero_quote_3_part1, R.string.hero_quote_3_part2),
        HeroQuote(R.string.hero_quote_4_part1, R.string.hero_quote_4_part2),
        HeroQuote(R.string.hero_quote_5_part1, R.string.hero_quote_5_part2),
        HeroQuote(R.string.hero_quote_6_part1, R.string.hero_quote_6_part2),
    )

    override val heroCaptions: List<Int> = listOf(
        R.string.hero_caption_1,
        R.string.hero_caption_2,
        R.string.hero_caption_3,
        R.string.hero_caption_4,
        R.string.hero_caption_5,
    )
}
