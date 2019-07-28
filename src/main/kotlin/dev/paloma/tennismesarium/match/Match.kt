package dev.paloma.tennismesarium.match

import dev.paloma.tennismesarium.player.Player
import java.util.*

sealed class Match {
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
        private val winner: Player?) : Match() {
    companion object {
        fun between(player1: Player, player2: Player): SinglesMatch {
            return SinglesMatch(UUID.randomUUID(), Pair(player1, player2), null)
        }
    }

    override fun toString(): String {
        return "${players.first} - ${players.second}"
    }

    override fun toJson(): Map<String, Any> {
        val output = LinkedHashMap<String, Any>()
        output["id"] = id.toString()
        output["players"] = listOf(players.first.toJson(), players.second.toJson())
        output["canBePlayed"] = canBePlayed()
        //TODO add results if available
        return output
    }

    private fun canBePlayed(): Boolean = (winner == null)
}