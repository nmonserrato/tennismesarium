package dev.paloma.tennismesarium.tournament

import dev.paloma.tennismesarium.match.Match
import dev.paloma.tennismesarium.match.Player

sealed class Tournament {
    abstract fun print()

    companion object {
        fun createSingleElimination(playerNames: List<String>): Tournament {

            return SingleEliminationTournament.generateBrackets("A tournament", playerNames.shuffled())
        }
    }
}

class SingleEliminationTournament private constructor(
        private val name: String,
        private val final: Round
) : Tournament() {

    companion object {
        fun generateBrackets(tournamentName: String, playerNames: List<String>): SingleEliminationTournament {
            assert(playerNames.isNotEmpty())
            assert(tournamentName.isNotBlank())
            return SingleEliminationTournament(tournamentName, generateRound(playerNames))
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

    override fun print() {
        println("Brackets for tournament: $name")
        final.printRecursively()
    }

}

sealed class Round {
    abstract fun printRecursively()
}

class SinglePlayerRound private constructor( private val player: Player) : Round() {
    override fun printRecursively() {
        println("${this.player}")
    }

    companion object {
        fun forPlayer(name: String): SinglePlayerRound {
            return SinglePlayerRound(Player(name))
        }
    }
}


class RegularMatchRound private constructor(
        private val match: Match?,
        private val previous: Pair<Round, Round>?
) : Round() {
    override fun printRecursively() {
        if (match != null) {
            println("$match")
        } else {
            println("Winner between")
        }
        if (previous != null) {
            previous.first.printRecursively()
            previous.second.printRecursively()
        }
    }

    companion object {
        fun forPlayers(player1Name: String, player2Name: String): RegularMatchRound {
            return RegularMatchRound(Match.between(player1Name, player2Name), null)
        }
        fun generatedBy(round1: Round, round2: Round): RegularMatchRound {
            return RegularMatchRound(null, Pair(round1, round2))
        }
    }
}
