package dev.paloma.tennismesarium.tournament

import dev.paloma.tennismesarium.match.Match
import dev.paloma.tennismesarium.player.Player

sealed class Round {
    abstract fun toJson(): Map<String, Any>
}

class SinglePlayerRound private constructor(private val player: Player) : Round() {
    companion object {
        fun forPlayer(player: Player): SinglePlayerRound {
            return SinglePlayerRound(player)
        }
    }

    override fun toJson(): Map<String, Any> {
        val output = LinkedHashMap<String, Any>()
        output["type"] = "SINGLE_PLAYER"
        output["player"] = "$player"
        return output
    }

    override fun toString(): String {
        return "$player"
    }
}


class RegularMatchRound private constructor(
        private val match: Match?,
        private val previous: Pair<Round, Round>?
) : Round() {
    companion object {
        fun forPlayers(player1: Player, player2: Player): RegularMatchRound {
            return RegularMatchRound(Match.between(player1, player2), null)
        }

        fun generatedBy(round1: Round, round2: Round): RegularMatchRound {
            return RegularMatchRound(null, Pair(round1, round2))
        }
    }

    override fun toJson(): Map<String, Any> {
        val previousRounds = ArrayList<Any>(2)
        previous?.first?.let { previousRounds.add(it.toJson()) }
        previous?.second?.let { previousRounds.add(it.toJson()) }

        val output = LinkedHashMap<String, Any>()
        output["type"] = "ELIMINATION_ROUND"
        output["previous"] = previousRounds
        match?.let { output["match"] = it.toJson() }
        return output
    }

    override fun toString(): String {
        return if (match != null) "$match"
        else "TBD"
    }
}