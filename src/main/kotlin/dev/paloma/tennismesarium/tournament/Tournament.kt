package dev.paloma.tennismesarium.tournament

import dev.paloma.tennismesarium.match.Match
import dev.paloma.tennismesarium.match.complete.MatchCompletedEvent
import dev.paloma.tennismesarium.player.Player
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap

sealed class Tournament (
        open val id: UUID,
        open val name: String,
        open val created: ZonedDateTime
) {
    abstract fun toJson(): Map<String, Any>

    abstract fun findPlayableMatch(matchId: UUID): Match?
    abstract fun findPlayableMatches(): List<Match>

    abstract fun updateRoundsAfterMatchCompleted()
    abstract fun isOver(): Boolean

    fun identifier() = id

    open fun basicInfo(): MutableMap<String, Any> {
        val output = LinkedHashMap<String, Any>()
        output["id"] = id.toString()
        output["name"] = name
        output["status"] = if (isOver()) "COMPLETED" else "IN PROGRESS"
        output["created"] = formatter.format(created)
        return output
    }

    // make it private once v2 is live?
    fun completeMatch(matchId: UUID, winnerId: UUID) {
        val match = findPlayableMatch(matchId)
                ?: throw IllegalArgumentException("No match $matchId found in tournament")
        match.complete(winnerId)
        updateRoundsAfterMatchCompleted()
    }


    companion object {
        val formatter: DateTimeFormatter = DateTimeFormatter.RFC_1123_DATE_TIME

        fun createSingleElimination(tournamentName: String, players: List<Player>): Tournament {
            return SingleEliminationTournament.generateBrackets(tournamentName, players.shuffled())
        }

        fun replayEvent(event: MatchCompletedEvent) {
            event.tournament.completeMatch(event.match.identifier(), event.winner.identifier())
        }
    }
}

class SingleEliminationTournament private constructor(
        override val id: UUID,
        override val name: String,
        private val final: Round,
        override val created: ZonedDateTime = ZonedDateTime.now()
) : Tournament(id, name, created) {
    companion object {
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

    override fun updateRoundsAfterMatchCompleted() {
        final.updateAfterMatchPlayed()
    }

    override fun toJson(): Map<String, Any> {
        val output = basicInfo()
        if(isOver()) output["winner"] = final.winner().toJson()
        output["finalRound"] = final.toJson()
        return output
    }

    override fun findPlayableMatch(matchId: UUID): Match? = final.findMatch(matchId)

    override fun findPlayableMatches() = final.findPlayableMatches()

    override fun isOver() = final.isCompleted()
}

class RoundRobinTournament private constructor(
        override val id: UUID,
        override val name: String,
        override val created: ZonedDateTime = ZonedDateTime.now(),
        private val rounds: List<RoundRobinRound> = ArrayList(),
        private var currentRoundIndex: Int = -1
) : Tournament(id, name, created) {
    companion object {
        fun generateRounds(name: String, players: List<Player>): RoundRobinTournament {
            val fakePlayer = Player(UUID.randomUUID(), "Fake")
            val simulationPlayers = players.toMutableList()
            if(players.size % 2 == 1) simulationPlayers.add(fakePlayer)
            val numOfRounds = (simulationPlayers.size - 1) * 2
            val numOfMatchesPerRound = simulationPlayers.size / 2
            val arrayOfPlayers = simulationPlayers.toTypedArray()
            val rounds = ArrayList<RoundRobinRound>(numOfRounds)
            // home
            for (r in 0 until numOfRounds / 2) {
                val matchesOfRound = ArrayList<Match>(numOfMatchesPerRound)
                for (m in 0 until numOfMatchesPerRound) {
                    if (fakePlayer in listOf(arrayOfPlayers[m], arrayOfPlayers[arrayOfPlayers.size - 1 - m]))
                        continue
                    matchesOfRound.add(Match.between(arrayOfPlayers[m], arrayOfPlayers[arrayOfPlayers.size - 1 - m]))
                }
                rounds.add(RoundRobinRound(r + 1, matchesOfRound))

                // rotate array
                val lastPlayer = arrayOfPlayers.last()
                for (i in (arrayOfPlayers.size - 2) downTo 1) {
                    arrayOfPlayers[i + 1] = arrayOfPlayers[i]
                }
                arrayOfPlayers[1] = lastPlayer
            }

            // away
            for (r in numOfRounds / 2 until numOfRounds) {
                val homeMatches = rounds[r - numOfRounds / 2].findPlayableMatches()
                val matchesOfRound = ArrayList<Match>(numOfMatchesPerRound)
                for (m in homeMatches) {
                    matchesOfRound.add(Match.between(m.players()[1], m.players()[0]))
                }
                rounds.add(RoundRobinRound(r + 1, matchesOfRound))
            }

            return RoundRobinTournament(id = UUID.randomUUID(), name = name, rounds = rounds, currentRoundIndex = 0)
        }

    }

    override fun updateRoundsAfterMatchCompleted() {
        if (currentRound().isCompleted())
            currentRoundIndex++
    }

    override fun toJson(): Map<String, Any> {
        val output = basicInfo()
        output["rounds"] = rounds.map { it.toJson() }.toList()
        return output
    }

    override fun findPlayableMatch(matchId: UUID) = currentRound().findMatch(matchId)

    override fun findPlayableMatches() = currentRound().findPlayableMatches()

    override fun isOver() = currentRoundIndex >= rounds.size

    private fun currentRound() = rounds[currentRoundIndex]
}