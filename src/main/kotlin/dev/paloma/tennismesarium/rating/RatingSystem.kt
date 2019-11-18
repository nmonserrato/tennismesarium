package dev.paloma.tennismesarium.rating

import dev.paloma.tennismesarium.rating.elo.Elo
import java.util.*

interface RatingSystem {
    companion object {
        private val ELO_INSTANCE = Elo()
        fun elo() = ELO_INSTANCE
    }

    fun reCalculateRatings(matches: List<MatchResult>)
    fun updateRatingsAfterMatch(match: MatchResult)
    fun getCurrentRatings(playerIds: Set<UUID>? = null): List<PlayerRating>
}

data class MatchResult (
        val playerOne: UUID,
        val playerTwo: UUID,
        val winner: UUID
)