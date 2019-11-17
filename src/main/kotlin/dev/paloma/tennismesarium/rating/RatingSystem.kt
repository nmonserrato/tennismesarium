package dev.paloma.tennismesarium.rating

import java.util.*

interface RatingSystem {
    fun reCalculateRatings(matches: List<MatchResult>) : List<PlayerRating>
    fun updateRatingsAfterMatch(match: MatchResult): List<PlayerRating>
    fun getCurrentRatings(): List<PlayerRating>
}

data class MatchResult (
        val playerOne: UUID,
        val playerTwo: UUID,
        val winner: UUID
)