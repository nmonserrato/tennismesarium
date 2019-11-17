package dev.paloma.tennismesarium.rating

import dev.paloma.tennismesarium.match.Match
import java.util.*

interface RatingSystem {
    fun reCalculateRatings(matches: List<Match>) : List<PlayerRating>
    fun updateRatingsAfterMatch(match: Match): List<PlayerRating>
}

data class MatchResult (
        val playerOne: UUID,
        val playerTwo: UUID,
        val winner: UUID
)