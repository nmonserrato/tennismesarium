package dev.paloma.tennismesarium.rating.elo

import dev.paloma.tennismesarium.player.Player
import dev.paloma.tennismesarium.rating.MatchResult
import dev.paloma.tennismesarium.rating.PlayerRating
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

internal class EloTest {

    private val p1 = UUID.randomUUID()
    private val p2 = UUID.randomUUID()
    private var elo  = Elo()

    @Test
    internal fun `expectation to win for two players with same score`() {
        setupPlayersWithRating(1500.0, 1500.0)

        assertEquals(0.5f, expectationToWin(), 0.01f)
    }

    @Test
    internal fun `expectation to win for two players with different score`() {
        setupPlayersWithRating(1700.0, 1300.0)

        assertEquals(0.9f, expectationToWin(), 0.01f)
    }

    @Test
    internal fun `new score after expected win`() {
        setupPlayersWithRating(1700.0, 1300.0)

        val match = MatchResult(p1, p2, p1)
        elo.updateRatingsAfterMatch(match)
        val newRatings = elo.getCurrentRatings()

        assertEquals(1702.0, newRating(newRatings, p1))
        assertEquals(1298.0, newRating(newRatings, p2))
    }

    @Test
    internal fun `new score after unexpected loss`() {
        setupPlayersWithRating(1700.0, 1300.0)

        val match = MatchResult(p1, p2, p2)
        elo.updateRatingsAfterMatch(match)
        val newRatings = elo.getCurrentRatings()

        assertEquals(1671.0, newRating(newRatings, p1))
        assertEquals(1329.0, newRating(newRatings, p2))
    }

    private fun setupPlayersWithRating(rating1: Double, rating2: Double) {
        elo = Elo.withRatings(
                mapOf(
                        p1 to PlayerRating(p1, rating1),
                        p2 to PlayerRating(p2, rating2)
                )
        )
    }

    private fun newRating(newRatings: List<PlayerRating>, playerId: UUID?) =
            newRatings.first { it.playerId == playerId }.rating

    private fun expectationToWin() =
            elo.expectationToWin(Player(p1, "p1"), Player(p2, "p2")).toFloat()

}