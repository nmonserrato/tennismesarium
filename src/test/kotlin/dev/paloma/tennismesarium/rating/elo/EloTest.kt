package dev.paloma.tennismesarium.rating.elo

import dev.paloma.tennismesarium.rating.PlayerRating
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

internal class EloTest {

    private val p1 = UUID.randomUUID()
    private val p2 = UUID.randomUUID()
    private val elo = Elo()

    @Test
    internal fun `expectation to win for two players with same score`() {
        val p1 = PlayerRating(p1, 1500.0)
        val p2 = PlayerRating(p2, 1500.0)

        assertEquals(0.5, elo.expectationToWin(p1, p2))
    }

    @Test
    internal fun `expectation to win for two players with different score`() {
        val p1 = PlayerRating(p1, 1700.0)
        val p2 = PlayerRating(p2, 1300.0)

        assertEquals(0.9f, elo.expectationToWin(p1, p2).toFloat(), 0.01f)
    }

    @Test
    internal fun `new score after unexpected loss`() {
        val p1 = PlayerRating(p1, 1700.0)
        val p2 = PlayerRating(p2, 1300.0)

        val (newRating1, newRating2) = elo.updateRatingsAfterMatch(p1, p2, p2.playerId)

        assertEquals(1671.0, newRating1.rating)
        assertEquals(1329.0, newRating2.rating)
    }
}