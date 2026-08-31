package com.example.core.data.model

/**
 * Domain model for a remotely-sourced hero quote (plain text).
 *
 * Distinct from the locale-resolved [com.example.core.data.repository.HeroQuote]
 * (which holds string-resource IDs for in-app language switching): remote content
 * arrives as already-localized text from the server.
 */
data class RemoteHeroQuote(
    val part1: String,
    val part2: String,
)
