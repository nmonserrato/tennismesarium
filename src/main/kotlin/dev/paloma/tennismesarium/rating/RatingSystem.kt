package dev.paloma.tennismesarium.rating

import dev.paloma.tennismesarium.match.Match
import java.util.*

interface RatingSystem {
    fun calculateRatings(matches: List<Match>)
}

data class MatchResult (
        val playerOne: UUID,
        val playerTwo: UUID,
        val winner: UUID
)