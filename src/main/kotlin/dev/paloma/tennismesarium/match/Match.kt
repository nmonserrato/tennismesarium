package dev.paloma.tennismesarium.match

import dev.paloma.tennismesarium.player.Player
import java.util.*

sealed class Match(
        private val id: UUID,
        private val games: List<Game>?
) {
    abstract fun toJson(): Map<String, Any>

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

    override fun toJson(): Map<String, Any> {
        val output = LinkedHashMap<String, Any>()
        output["id"] = id.toString()
        output["players"] = listOf(players.first.toString(), players.second.toString())
        output["canBePlayed"] = canBePlayed()
        //TODO add results if available
        return output
    }

    private fun canBePlayed(): Boolean = games.isEmpty()
}