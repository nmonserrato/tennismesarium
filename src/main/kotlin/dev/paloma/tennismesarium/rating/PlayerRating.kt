package dev.paloma.tennismesarium.rating

import java.util.*

data class PlayerRating (
        val playerId: UUID,
        val rating: Double,
        val lastIncrement: Double = 0.0
)