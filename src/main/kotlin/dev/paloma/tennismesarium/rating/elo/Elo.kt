package dev.paloma.tennismesarium.rating.elo

import dev.paloma.tennismesarium.player.Player
import dev.paloma.tennismesarium.rating.MatchResult
import dev.paloma.tennismesarium.rating.PlayerRating
import dev.paloma.tennismesarium.rating.RatingSystem
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.pow

class Elo : RatingSystem {
    companion object {
        private const val K = 32
        private const val STARTING_SCORE = 1200.0

        fun withRatings(ratings: Map<UUID, PlayerRating>): Elo {
            val elo = Elo()
            elo.currentRatings.putAll(ratings)
            return elo
        }

    }

    private val currentRatings = HashMap<UUID, PlayerRating>()

    override fun updateRatingsAfterMatch(match: MatchResult) {
        val p1 = match.playerOne
        val p2 = match.playerTwo

        val (newP1, newP2) = updateRatingsAfterMatch(scoreFor(p1), scoreFor(p2), match.winner)

        currentRatings[p1] = newP1
        currentRatings[p2] = newP2
    }

    override fun reCalculateRatings(matches: List<MatchResult>) {
        currentRatings.clear()
        matches.forEach { updateRatingsAfterMatch(it) }
    }

    override fun getCurrentRatings(keys: Set<UUID>?): List<PlayerRating> =
            (keys ?: currentRatings.keys)
                    .map { scoreFor(it) }
                    .sortedByDescending { it.rating }
                    .toList()

    fun expectationToWin(player1: Player, player2: Player) = expectationToWin(
            scoreFor(player1.identifier()), scoreFor(player2.identifier())
    )

    private fun updateRatingsAfterMatch(p1: PlayerRating, p2: PlayerRating, winner: UUID): Pair<PlayerRating, PlayerRating> {
        val outcome = if (p1.playerId == winner) 1.0 else 0.0
        val delta = (K * (outcome - expectationToWin(p1, p2))).toInt()
        val newP1Rating = PlayerRating(p1.playerId, p1.rating + delta, delta.toDouble())
        val newP2Rating = PlayerRating(p2.playerId, p2.rating - delta, (-delta).toDouble())
        return Pair(newP1Rating, newP2Rating)
    }

    private fun scoreFor(player: UUID) = currentRatings.getOrDefault(player, startingRating(player))

    private fun startingRating(p1Id: UUID) = PlayerRating(p1Id, STARTING_SCORE, 0.0)

    private fun expectationToWin(player1: PlayerRating, player2: PlayerRating) =
            1.0 / (1.0 + 10.0.pow((player2.rating - player1.rating) / 400.0))
}