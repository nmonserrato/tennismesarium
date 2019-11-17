package dev.paloma.tennismesarium.rating.elo

import dev.paloma.tennismesarium.match.Match
import dev.paloma.tennismesarium.rating.PlayerRating
import dev.paloma.tennismesarium.rating.RatingSystem
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.pow

class Elo : RatingSystem {
    companion object {
        private const val K = 32
        private const val STARTING_SCORE = 1200.0
    }

    private val currentRatings = HashMap<UUID, PlayerRating>()

    override fun updateRatingsAfterMatch(match: Match): List<PlayerRating> {
        val p1 = match.players()[0].identifier()
        val p2 = match.players()[2].identifier()

        val (newP1, newP2) = updateRatingsAfterMatch(scoreFor(p1), scoreFor(p2), match.winner().identifier())

        currentRatings[p1] = newP1
        currentRatings[p2] = newP2

        return toNiceList()
    }

    override fun reCalculateRatings(matches: List<Match>): List<PlayerRating> {
        currentRatings.clear()
        matches.forEach { updateRatingsAfterMatch(it) }
        return toNiceList()
    }

    private fun scoreFor(p1Id: UUID) = currentRatings.getOrDefault(p1Id, PlayerRating(p1Id, STARTING_SCORE))

    private fun toNiceList() = currentRatings.values.sortedByDescending { it.rating }.toList()

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