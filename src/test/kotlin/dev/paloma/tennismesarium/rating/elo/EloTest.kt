package dev.paloma.tennismesarium.rating.elo

import dev.paloma.tennismesarium.player.Player
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
        val p1 = PlayerRating(p1, 1700.0)
        val p2 = PlayerRating(p2, 1300.0)

        val (newRating1, newRating2) = elo.updateRatingsAfterMatch(p1, p2, p1.playerId)

        assertEquals(1702.0, newRating1.rating)
        assertEquals(1298.0, newRating2.rating)
    }

    @Test
    internal fun `new score after unexpected loss`() {
        val p1 = PlayerRating(p1, 1700.0)
        val p2 = PlayerRating(p2, 1300.0)

        val (newRating1, newRating2) = elo.updateRatingsAfterMatch(p1, p2, p2.playerId)

        assertEquals(1671.0, newRating1.rating)
        assertEquals(1329.0, newRating2.rating)
    }

    private fun setupPlayersWithRating(rating1: Double, rating2: Double) {
        elo = Elo.withRatings(
                mapOf(
                        p1 to PlayerRating(p1, rating1),
                        p2 to PlayerRating(p2, rating2)
                )
        )
    }

    private fun expectationToWin() =
            elo.expectationToWin(Player(p1, "p1"), Player(p2, "p2")).toFloat()

}