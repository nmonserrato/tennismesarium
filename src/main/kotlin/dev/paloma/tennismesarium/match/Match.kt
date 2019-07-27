package dev.paloma.tennismesarium.match

import dev.paloma.tennismesarium.player.Player
import java.util.*

sealed class Match(
        private val id: UUID,
        private val games: List<Game>?
) {
    abstract fun canBePlayed(): Boolean
    abstract fun basicInfo(): Map<String, String>

    companion object {
        fun between(player1: Player, player2: Player): Match {
            return SinglesMatch.between(player1, player2)
        }
    }
}

class SinglesMatch(
        private val id: UUID,
        private val players: Pair<Player, Player>,
        private val games: List<Game>
) : Match(id, games) {
    companion object {
        fun between(player1: Player, player2: Player): SinglesMatch {
            return SinglesMatch(UUID.randomUUID(), Pair(player1, player2), emptyList())
        }
    }

    override fun toString(): String {
        return "${players.first} - ${players.second}"
    }

    override fun canBePlayed(): Boolean = games.isEmpty()

    override fun basicInfo(): Map<String, String> = mapOf(
            "identifier" to id.toString(),
            "player1" to players.first.toString(),
            "player2" to players.second.toString()
    )
}