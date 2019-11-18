package dev.paloma.tennismesarium.tournament

import dev.paloma.tennismesarium.player.Player
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

internal class RoundRobinTournamentTest {

    @Test
    fun tournamentWithFourPlayers() {
        val players = players("Nino", "Diego", "Germanno", "Alejandro")

        val tournament = RoundRobinTournament.generateRounds("Test Name", players, true)
        val rounds = tournament.toJson()["rounds"] as List<Map<String, Any>>

        assertEquals(tournament.name, "Test Name")

        assertThatRoundGamesWere(rounds[0], listOf(listOf("Nino", "Alejandro"), listOf("Diego", "Germanno")))
        assertThatRoundGamesWere(rounds[1], listOf(listOf("Nino", "Germanno"), listOf("Alejandro", "Diego")))
        assertThatRoundGamesWere(rounds[2], listOf(listOf("Nino", "Diego"), listOf("Germanno", "Alejandro")))
        assertThatRoundGamesWere(rounds[3], listOf(listOf("Alejandro", "Nino"), listOf("Germanno", "Diego")))
        assertThatRoundGamesWere(rounds[4], listOf(listOf("Germanno", "Nino"), listOf("Diego", "Alejandro")))
        assertThatRoundGamesWere(rounds[5], listOf(listOf("Diego", "Nino"), listOf("Alejandro", "Germanno")))
        verifyThatEachPlayersPlayedAgainstEachOther(rounds, players, 2)
    }

    @Test
    fun tournamentWithFourPlayersAndNoReturnMatches() {
        val players = players("Nino", "Diego", "Germanno", "Alejandro")

        val tournament = RoundRobinTournament.generateRounds("Test Name", players, false)
        val rounds = tournament.toJson()["rounds"] as List<Map<String, Any>>

        assertEquals(tournament.name, "Test Name")

        assertEquals(3, rounds.size)
        assertThatRoundGamesWere(rounds[0], listOf(listOf("Nino", "Alejandro"), listOf("Diego", "Germanno")))
        assertThatRoundGamesWere(rounds[1], listOf(listOf("Nino", "Germanno"), listOf("Alejandro", "Diego")))
        assertThatRoundGamesWere(rounds[2], listOf(listOf("Nino", "Diego"), listOf("Germanno", "Alejandro")))
        verifyThatEachPlayersPlayedAgainstEachOther(rounds, players, 1)
    }

    @Test
    fun tournamentWithFivePlayers() {
        val players = players("A", "B", "C", "D", "E")

        val tournament = RoundRobinTournament.generateRounds("Test Name", players, true)
        val rounds = tournament.toJson()["rounds"] as List<Map<String, Any>>

        assertEquals(tournament.name, "Test Name")

        assertThatRoundGamesWere(rounds[0], listOf(listOf("B", "E"), listOf("C", "D")))
        assertThatRoundGamesWere(rounds[1], listOf(listOf("A", "E"), listOf("B", "C")))
        assertThatRoundGamesWere(rounds[2], listOf(listOf("A", "D"), listOf("E", "C")))
        assertThatRoundGamesWere(rounds[3], listOf(listOf("A", "C"), listOf("D", "B")))
        assertThatRoundGamesWere(rounds[4], listOf(listOf("A", "B"), listOf("D", "E")))
    }

    @Test
    fun tournamentWith20Players() {
        val players = (1..20).map { Player(UUID.randomUUID(), "p$it") }.toList()

        val tournament = RoundRobinTournament.generateRounds("Test Name", players, true)
        val rounds = tournament.toJson()["rounds"] as List<Map<String, Any>>

        assertEquals(tournament.name, "Test Name")
        assertEquals(38, rounds.size)
        verifyThatEachPlayersPlayedAgainstEachOther(rounds, players, 2)
    }

    private fun verifyThatEachPlayersPlayedAgainstEachOther(rounds: List<Map<String, Any>>, players: List<Player>, times: Int) {
        val groupedMatched = rounds
                .flatMap { it["matches"] as List<Map<String, Any>> }
                .asSequence()
                .map { it["players"] as List<Map<String, String>> }
                .map { listOf(it[0]["name"] as String, it[1]["name"] as String).sorted() }
                .map { "${it[0]} ${it[1]}" }
                .sorted()
                .groupingBy { it }
                .eachCount()

        for (p1 in players) {
            for (p2 in players) {
                if (p1 == p2)
                    continue
                else
                    assertEquals(times, groupedMatched.getOrElse("$p1 $p2", { groupedMatched["$p2 $p1"] }))
            }
        }
    }

    private fun assertThatRoundGamesWere(round: Map<String, Any>, expectedMatches: List<List<String>>) {
        val actualMatches = round["matches"] as List<Map<String, Any>>
        assertEquals(expectedMatches.size, actualMatches.size)
        val matchAndPlayers = actualMatches
                .map { it["players"] as List<Map<String, String>> }
                .map { listOf(it[0]["name"] as String, it[1]["name"] as String) }
                .toList()

        assertEquals(matchAndPlayers, expectedMatches)
    }

    private fun players(vararg names: String): List<Player> =
            names
                .map { Player(UUID.randomUUID(), it) }
                .toList()
}