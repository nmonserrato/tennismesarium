package dev.paloma.tennismesarium.rating.glicko2

import java.util.*

data class PlayerRating (
        val playerId: UUID,
        val rating: Double = 0.toDouble(),
        val ratingDeviation: Double = 0.toDouble(),
        val volatility: Double = 0.toDouble()
)