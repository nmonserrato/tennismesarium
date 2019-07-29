package dev.paloma.tennismesarium

import dev.paloma.tennismesarium.player.InMemoryPlayersRepository
import dev.paloma.tennismesarium.tournament.SingleEliminationTournament
import dev.paloma.tennismesarium.tournament.Tournament
import org.junit.Test


class TennismesariumApplicationTests {

	@Test
	fun `serialize tournament`() {
		val players = InMemoryPlayersRepository().createAll(listOf("A", "B", "C", "d", "e", "f", "g", "h", "i"))
		val tournament = Tournament.createSingleElimination("Tomoki Cup", players)

		val json = tournament.toJson()

		val parsed = SingleEliminationTournament.fromJson(json)

		println(parsed)
	}

}
