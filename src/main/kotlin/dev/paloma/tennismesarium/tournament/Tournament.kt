package dev.paloma.tennismesarium.tournament

import dev.paloma.tennismesarium.match.Match
import dev.paloma.tennismesarium.player.Player
import java.lang.IllegalArgumentException
import java.util.*
import kotlin.collections.LinkedHashMap

sealed class Tournament {
    abstract fun identifier(): UUID
    abstract fun completeMatch(matchId: UUID, winnerId: UUID)
    abstract fun toJson(): Map<String, Any>

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

    override fun completeMatch(matchId: UUID, winnerId: UUID) {
        val match = findPlayableMatch(matchId)
                ?: throw IllegalArgumentException("No match $matchId found in tournament")
        match.complete(winnerId)
        final.onMatchCompleted(matchId)
    }

    override fun toJson(): Map<String, Any> {
        val output = LinkedHashMap<String, Any>()
        output["id"] = id.toString()
        output["name"] = name
        output["finalRound"] = final.toJson()
        return output
    }

    private fun findPlayableMatch(matchId: UUID): Match? = final.findMatch(matchId)

}