package dev.paloma.tennismesarium.tournament

import org.junit.jupiter.api.Test

internal class SingleEliminationTournamentTest {
    @Test
    fun createSingleMatch() {
        val players = listOf("Juan", "Anthony", "Pino", "Katiusha", "Carlo", "Juana", "Laia", "Miquel", "Stephan")
        val tournament = Tournament.createSingleElimination(players)
        tournament.print()
    }
}