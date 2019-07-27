package dev.paloma.tennismesarium.tournament

import dev.paloma.tennismesarium.match.Match
import dev.paloma.tennismesarium.match.Player
import dev.paloma.tennismesarium.tournament.printing.SingleEliminationTournamentPrinter2
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.util.*

sealed class Tournament {
    abstract fun printBrackets(): String
    abstract fun identifier(): UUID

    companion object {
        fun createSingleElimination(tournamentName: String, playerNames: List<String>): Tournament {
            return SingleEliminationTournament.generateBrackets(tournamentName, playerNames.shuffled())
        }
    }
}

class SingleEliminationTournament private constructor(
        private val id: UUID,
        private val name: String,
        private val final: Round
) : Tournament() {
    companion object {
        fun generateBrackets(tournamentName: String, playerNames: List<String>): SingleEliminationTournament {
            assert(playerNames.isNotEmpty())
            assert(tournamentName.isNotBlank())
            return SingleEliminationTournament(UUID.randomUUID(), tournamentName, generateRound(playerNames))
        }

        private fun generateRound(playerNames: List<String>): Round {
            return when {
                playerNames.size == 1 -> SinglePlayerRound.forPlayer(playerNames[0])
                playerNames.size == 2 -> RegularMatchRound.forPlayers(playerNames[0], playerNames[1])
                else -> {
                    val halfIndex = playerNames.size / 2
                    val half1 = playerNames.subList(0, halfIndex)
                    val half2 = playerNames.subList(halfIndex, playerNames.size)
                    RegularMatchRound.generatedBy(generateRound(half1), generateRound(half2))
                }
            }

        }
    }

    override fun identifier() = id

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
}

class SinglePlayerRound private constructor(private val player: Player) : Round() {
    companion object {
        fun forPlayer(name: String): SinglePlayerRound {
            return SinglePlayerRound(Player(name))
        }
    }

    override fun getLeft(): Round? = null

    override fun getRight(): Round? = null

    override fun toString(): String {
        return "$player"
    }
}


class RegularMatchRound private constructor(
        private val match: Match?,
        private val previous: Pair<Round, Round>?
) : Round() {
    companion object {
        fun forPlayers(player1Name: String, player2Name: String): RegularMatchRound {
            return RegularMatchRound(Match.between(player1Name, player2Name), null)
        }

        fun generatedBy(round1: Round, round2: Round): RegularMatchRound {
            return RegularMatchRound(null, Pair(round1, round2))
        }
    }

    override fun getLeft(): Round? = previous?.first

    override fun getRight(): Round? = previous?.second

    override fun toString(): String {
        return if (match != null) "$match"
        else "TBD"
    }
}