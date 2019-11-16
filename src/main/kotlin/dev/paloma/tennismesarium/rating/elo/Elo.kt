package dev.paloma.tennismesarium.rating.elo

import dev.paloma.tennismesarium.match.Match
import dev.paloma.tennismesarium.rating.PlayerRating
import dev.paloma.tennismesarium.rating.RatingSystem
import java.util.*
import kotlin.math.pow

class Elo : RatingSystem {
    companion object {
        private const val K = 32;
    }

    override fun calculateRatings(matches: List<Match>): List<PlayerRating> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun expectationToWin(player1: PlayerRating, player2: PlayerRating) =
        1.0 / (1.0 + 10.0.pow((player2.rating - player1.rating) / 400.0))

    fun updateRatingsAfterMatch(p1: PlayerRating, p2: PlayerRating, winner: UUID): Pair<PlayerRating, PlayerRating> {
        val outcome = if (p1.playerId == winner) 1.0 else 0.0
        val delta = (K * (outcome - expectationToWin(p1, p2))).toInt()
        val newP1Rating = PlayerRating(p1.playerId, p1.rating + delta)
        val newP2Rating = PlayerRating(p2.playerId, p2.rating - delta)
        return Pair(newP1Rating, newP2Rating)
    }
}