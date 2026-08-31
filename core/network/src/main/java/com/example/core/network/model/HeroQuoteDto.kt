package com.example.core.network.model

import kotlinx.serialization.Serializable

/**
 * Network DTO for a hero quote, as returned by the remote greeting API.
 * Kept separate from domain models so the wire format can evolve independently.
 */
@Serializable
data class HeroQuoteDto(
    val part1: String,
    val part2: String,
)
