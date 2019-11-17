package dev.paloma.tennismesarium.rating

import dev.paloma.tennismesarium.rating.elo.Elo
import java.util.*

interface RatingSystem {
    fun reCalculateRatings(matches: List<MatchResult>) : List<PlayerRating>
    fun updateRatingsAfterMatch(match: MatchResult): List<PlayerRating>
    fun getCurrentRatings(): List<PlayerRating>

    companion object {
        private val ELO_INSTANCE = Elo()
        fun elo() = ELO_INSTANCE
    }
}

data class MatchResult (
        val playerOne: UUID,
        val playerTwo: UUID,
        val winner: UUID
)