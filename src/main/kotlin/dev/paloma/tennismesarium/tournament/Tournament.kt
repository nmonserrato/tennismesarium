package dev.paloma.tennismesarium.tournament

import dev.paloma.tennismesarium.match.Match
import dev.paloma.tennismesarium.match.complete.MatchCompletedEvent
import dev.paloma.tennismesarium.player.Player
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

sealed class Tournament {
    abstract fun identifier(): UUID
    abstract fun completeMatch(matchId: UUID, winnerId: UUID)
    abstract fun toJson(): Map<String, Any>
    abstract fun basicInfo(): Map<String, Any>
    abstract fun findPlayableMatch(matchId: UUID): Match?
    abstract fun findPlayableMatches(): List<Match>

    companion object {
        fun createSingleElimination(tournamentName: String, players: List<Player>): Tournament {
            return SingleEliminationTournament.generateBrackets(tournamentName, players.shuffled())
        }

        fun replayEvent(event: MatchCompletedEvent) {
            event.tournament.completeMatch(event.match.identifier(), event.winner.identifier())
        }
    }
}

class SingleEliminationTournament private constructor(
        private val id: UUID,
        private val name: String,
        private val final: Round,
        private val created: ZonedDateTime = ZonedDateTime.now()
) : Tournament() {
    companion object {
        private val formatter: DateTimeFormatter = DateTimeFormatter.RFC_1123_DATE_TIME

        val DESCENDING_CREATION_DATE = compareByDescending<Tournament> { (it as SingleEliminationTournament).created }

        fun fromJson(json: Map<String, Any>): Tournament {
            val final = Round.fromJSON(json["finalRound"] as Map<String, Any>)
            val id = UUID.fromString(json["id"] as String)
            val name = json["name"] as String
            val created = ZonedDateTime.parse(json["created"] as String, formatter)
            return SingleEliminationTournament(id, name, final, created)
        }

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

    // make it private once v2 is live?
    override fun completeMatch(matchId: UUID, winnerId: UUID) {
        val match = findPlayableMatch(matchId)
                ?: throw IllegalArgumentException("No match $matchId found in tournament")
        match.complete(winnerId)
        final.onMatchCompleted(matchId)
    }

    override fun toJson(): Map<String, Any> {
        val output = basicInfo()
        if(final.isCompleted()) output["winner"] = final.winner().toJson()
        output["finalRound"] = final.toJson()
        return output
    }

    override fun basicInfo(): LinkedHashMap<String, Any> {
        val output = LinkedHashMap<String, Any>()
        output["id"] = id.toString()
        output["name"] = name
        output["created"] = formatter.format(created)
        output["status"] = if (final.isCompleted()) "COMPLETED" else "IN PROGRESS"
        return output
    }

    override fun findPlayableMatch(matchId: UUID): Match? = final.findMatch(matchId)

    override fun findPlayableMatches() = final.findPlayableMatches()
}
