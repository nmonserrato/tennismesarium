package dev.paloma.tennismesarium.tournament

import dev.paloma.tennismesarium.match.Match
import dev.paloma.tennismesarium.player.Player
import java.lang.IllegalStateException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap

sealed class Round {
    abstract fun toJson(): Map<String, Any>
    abstract fun findMatch(matchId: UUID): Match?
    abstract fun onMatchCompleted(matchId: UUID)
    abstract fun isCompleted(): Boolean
    abstract fun winner(): Player

    companion object {
        fun fromJSON(json: Map<String, Any>): Round {
            return if(json["type"] == "SINGLE_PLAYER")
                SinglePlayerRound.fromJSON(json)
            else
                RegularMatchRound.fromJSON(json)
        }
    }
}

class SinglePlayerRound private constructor(private val player: Player) : Round() {
    companion object {
        fun forPlayer(player: Player): SinglePlayerRound {
            return SinglePlayerRound(player)
        }

        fun fromJSON(json: Map<String, Any>): SinglePlayerRound {
            val player = Player.fromJSON(json["player"] as Map<String, Any>)
            return forPlayer(player)
        }
    }

    override fun findMatch(matchId: UUID): Match? = null

    override fun isCompleted() = true

    override fun onMatchCompleted(matchId: UUID) {}

    override fun winner() = player

    override fun toJson(): Map<String, Any> {
        val output = LinkedHashMap<String, Any>()
        output["type"] = "SINGLE_PLAYER"
        output["player"] = player.toJson()
        return output
    }

    override fun toString(): String {
        return "$player"
    }
}

class RegularMatchRound private constructor(
        private var match: Match?,
        private val previous: Pair<Round, Round>?
) : Round() {
    companion object {
        fun forPlayers(player1: Player, player2: Player): RegularMatchRound {
            return RegularMatchRound(Match.between(player1, player2), null)
        }

        fun generatedBy(round1: Round, round2: Round): RegularMatchRound {
            return RegularMatchRound(null, Pair(round1, round2))
        }


        fun fromJSON(json: Map<String, Any>): RegularMatchRound {
            var previousRounds: Pair<Round, Round>? = null
            if (json["previous"] != null) {
                val listRoundsJson = json["previous"] as List<Map<String, Any>>
                val listRounds = listRoundsJson.map { Round.fromJSON(it) }.toList()
                if (listRounds.isNotEmpty())
                    previousRounds = Pair(listRounds[0], listRounds[1])
            }
            val match = json["match"]?.let { Match.fromJSON(it as Map<String, Any>) }

            return RegularMatchRound(match, previousRounds)
        }
    }

    override fun findMatch(matchId: UUID): Match? {
        if (matchId == match?.identifier())
            return match

        previous?.first?.findMatch(matchId)?.let { return it }
        previous?.second?.findMatch(matchId)?.let { return it }

        return null
    }

    override fun isCompleted() = match?.isCompleted() == true

    override fun winner() = match?.winner() ?: throw IllegalStateException("Round is not complete yet")

    override fun onMatchCompleted(matchId: UUID) {
        if (match != null) return

        if (previous?.first?.isCompleted() == true && previous.second.isCompleted()) {
            match = Match.between(previous.first.winner(), previous.second.winner())
        }

        previous?.first?.onMatchCompleted(matchId)
        previous?.second?.onMatchCompleted(matchId)

        return
    }

    override fun toJson(): Map<String, Any> {
        val previousRounds = ArrayList<Any>(2)
        previous?.first?.let { previousRounds.add(it.toJson()) }
        previous?.second?.let { previousRounds.add(it.toJson()) }

        val output = LinkedHashMap<String, Any>()
        output["type"] = "ELIMINATION_ROUND"
        match?.let { output["match"] = it.toJson() }
        output["previous"] = previousRounds
        return output
    }

    override fun toString(): String {
        return if (match != null) "$match"
        else "TBD"
    }
}