package dev.paloma.tennismesarium.match

import dev.paloma.tennismesarium.player.Player
import dev.paloma.tennismesarium.rating.MatchResult
import dev.paloma.tennismesarium.rating.RatingSystem
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.util.*

sealed class Match {
    abstract fun identifier(): UUID
    abstract fun toJson(): Map<String, Any>
    abstract fun complete(winnerId: UUID)
    abstract fun skip()
    abstract fun hasBeenPlayed(): Boolean
    abstract fun isCompleted(): Boolean
    abstract fun winner(): Player
    abstract fun players(): List<Player>
    abstract fun result(): MatchResult

    companion object {
        fun between(player1: Player, player2: Player): Match {
            return SinglesMatch.between(player1, player2)
        }

        fun fromJSON(json: Map<String, Any>): Match {
            return SinglesMatch.fromJSON(json)
        }
    }
}

class SinglesMatch(
        private val id: UUID,
        private val players: Pair<Player, Player>,
        private var winner: Player?,
        private var skipped: Boolean = false) : Match() {
    companion object {
        fun between(player1: Player, player2: Player): SinglesMatch {
            return SinglesMatch(UUID.randomUUID(), Pair(player1, player2), null)
        }

        fun fromJSON(json: Map<String, Any>): Match {
            val id = UUID.fromString(json["id"] as String)
            val players = (json["players"] as List<Map<String, Any>>).map { Player.fromJSON(it) }.zipWithNext().first()
            val winner = json["winner"]?.let { Player.fromJSON(it as  Map<String, Any>) }
            val skipped = (json["skipped"] as Boolean?) ?: false
            return SinglesMatch(id, players, winner, skipped)
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
        output["canBePlayed"] = !isCompleted()
        output["skipped"] = skipped
        if (!isCompleted()) output["expectation"] =
                RatingSystem.elo().expectationToWin(players.first, players.second).times(100).toInt()
        winner?.let { output["winner"] = it.toJson() }
        return output
    }

    override fun isCompleted() = (winner != null) || skipped
    override fun hasBeenPlayed() = (winner != null)

    override fun winner() = winner ?: throw IllegalStateException("Match $id has no winner")

    override fun skip() {
        if (isCompleted())
            throw IllegalStateException("Match was already completed")

        skipped = true
    }

    override fun complete(winnerId: UUID) {
        when (winnerId) {
            players.first.identifier() -> this.winner = players.first
            players.second.identifier() -> this.winner = players.second
            else -> throw  IllegalArgumentException("Player ${winnerId.toString()} is not playing match $id")
        }
    }

    override fun players(): List<Player> = listOf(players.first, players.second)

    override fun result(): MatchResult = MatchResult(
            this.players.first.identifier(),
            this.players.second.identifier(),
            this.winner!!.identifier()
    )
}
