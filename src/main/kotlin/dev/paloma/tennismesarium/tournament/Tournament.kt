package dev.paloma.tennismesarium.tournament

import dev.paloma.tennismesarium.match.Match
import dev.paloma.tennismesarium.player.Player
import dev.paloma.tennismesarium.tournament.printing.SingleEliminationTournamentPrinter2
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.util.*
import kotlin.collections.ArrayList

sealed class Tournament {
    abstract fun printBrackets(): String
    abstract fun identifier(): UUID
    abstract fun findNextPlayableMatches(): List<Match>

    companion object {
        fun createSingleElimination(tournamentName: String, players: List<Player>): Tournament {
            return SingleEliminationTournament.generateBrackets(tournamentName, players.shuffled())
        }
    }
}

class SingleEliminationTournament private constructor(
        private val id: UUID,
        private val name: String,
        private val final: Round
) : Tournament() {
    companion object {
        fun generateBrackets(tournamentName: String, players: List<Player>): SingleEliminationTournament {
            assert(players.isNotEmpty())
            assert(tournamentName.isNotBlank())
            return SingleEliminationTournament(UUID.randomUUID(), tournamentName, generateRound(players))
        }

        private fun generateRound(players: List<Player>): Round {
            return when {
                players.size == 1 -> SinglePlayerRound.forPlayer(players[0])
                players.size == 2 -> RegularMatchRound.forPlayers(players[0], players[1])
                else -> {
                    val halfIndex = players.size / 2
                    val half1 = players.subList(0, halfIndex)
                    val half2 = players.subList(halfIndex, players.size)
                    RegularMatchRound.generatedBy(generateRound(half1), generateRound(half2))
                }
            }

        }
    }

    override fun identifier() = id

    override fun findNextPlayableMatches(): List<Match> = final.findPlayableMatches()

    override fun printBrackets(): String {
        val stream = ByteArrayOutputStream()
        PrintStream(stream, true, "UTF-8")
                .use {
                    it.println("Brackets for tournament: $name")
                    SingleEliminationTournamentPrinter2().print(final, it)
                }
        return String(stream.toByteArray())
    }
}

sealed class Round {
    //TODO these are here only for printing. Remove?
    abstract fun getLeft(): Round?

    abstract fun getRight(): Round?

    abstract fun findPlayableMatches(): List<Match>
}

class SinglePlayerRound private constructor(private val player: Player) : Round() {
    companion object {
        fun forPlayer(player: Player): SinglePlayerRound {
            return SinglePlayerRound(player)
        }
    }

    override fun getLeft(): Round? = null
    override fun getRight(): Round? = null

    override fun findPlayableMatches(): List<Match> = emptyList()

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

    override fun getLeft(): Round? = previous?.first
    override fun getRight(): Round? = previous?.second

    override fun findPlayableMatches(): List<Match> {
        val matches = ArrayList<Match>()
        previous?.first?.findPlayableMatches()?.forEach { matches.add(it) }
        if (match?.canBePlayed() == true) matches.add(match)
        previous?.second?.findPlayableMatches()?.forEach { matches.add(it) }
        return matches
    }

    override fun toString(): String {
        return if (match != null) "$match"
        else "TBD"
    }
}