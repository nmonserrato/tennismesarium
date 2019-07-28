package dev.paloma.tennismesarium.match

import dev.paloma.tennismesarium.player.Player
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.util.*

sealed class Match {
    abstract fun identifier(): UUID
    abstract fun toJson(): Map<String, Any>
    abstract fun complete(winnerId: UUID)
    abstract fun isCompleted(): Boolean
    abstract fun winner(): Player

    companion object {
        fun between(player1: Player, player2: Player): Match {
            return SinglesMatch.between(player1, player2)
        }
    }
}

class SinglesMatch(
        private val id: UUID,
        private val players: Pair<Player, Player>,
        private var winner: Player?) : Match() {

    companion object {
        fun between(player1: Player, player2: Player): SinglesMatch {
            return SinglesMatch(UUID.randomUUID(), Pair(player1, player2), null)
        }
    }

    override fun identifier() = id

    override fun toString(): String {
        return "${players.first} - ${players.second}"
    }

    override fun toJson(): Map<String, Any> {
        val output = LinkedHashMap<String, Any>()
        output["id"] = id.toString()
        output["players"] = listOf(players.first.toJson(), players.second.toJson())
        output["canBePlayed"] = canBePlayed()
        winner?.let { output["winner"] = it.toJson() }
        return output
    }

    private fun canBePlayed(): Boolean = (winner == null)

    override fun isCompleted() = (winner != null)

    override fun winner() = winner ?: throw IllegalStateException("Match $id has not been played")

    override fun complete(winnerId: UUID) {
        when (winnerId) {
            players.first.identifier() -> this.winner = players.first
            players.second.identifier() -> this.winner = players.second
            else -> throw  IllegalArgumentException("Player ${winnerId.toString()} is not playing match $id")
        }
    }
}