package dev.paloma.tennismesarium.tournament

import dev.paloma.tennismesarium.match.Match
import dev.paloma.tennismesarium.match.complete.MatchCompletedEvent
import dev.paloma.tennismesarium.player.Player
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashMap

sealed class Tournament (
        open val id: UUID,
        open val name: String,
        open val created: ZonedDateTime,
        open val type: String
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
        output["mode"] = type
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

        fun createFixtures(tournamentName: String, players: List<Player>): Tournament {
            return RoundRobinTournament.generateRounds(tournamentName, players)
        }

        fun replayEvent(event: MatchCompletedEvent) {
            event.tournament.completeMatch(event.match.identifier(), event.winner.identifier())
        }

        fun fromJson(json: Map<String, Any>): Tournament {
            return if (json["mode"] == "SINGLE_ELIMINATION")
                SingleEliminationTournament.fromJson(json)
            else
                RoundRobinTournament.fromJson(json)
        }
    }
}

class SingleEliminationTournament private constructor(
        override val id: UUID,
        override val name: String,
        private val final: Round,
        override val created: ZonedDateTime = ZonedDateTime.now()
) : Tournament(id, name, created, "SINGLE_ELIMINATION") {
    companion object {
        val DESCENDING_CREATION_DATE = compareByDescending<Tournament> { it.created }

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
        private var currentRoundIndex: Int = 0,
        private val tables: MutableMap<String, ResultsSummary> = HashMap()
) : Tournament(id, name, created, "FIXTURES") {
    init {
        recalculateTables()
    }

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

            return RoundRobinTournament(id = UUID.randomUUID(), name = name, rounds = rounds)
        }

        fun fromJson(json: Map<String, Any>): Tournament {
            val id = UUID.fromString(json["id"] as String)
            val name = json["name"] as String
            val created = ZonedDateTime.parse(json["created"] as String, formatter)
            val currentRound = json["currentRound"] as Int
            val rounds = (json["rounds"] as List<Map<String, Any>>)
                    .map { RoundRobinRound.fromJson(it) }
                    .toList()
            return RoundRobinTournament(id, name, created, rounds, currentRound)
        }

    }

    override fun updateRoundsAfterMatchCompleted() {
        if (currentRound().isCompleted())
            currentRoundIndex++
        recalculateTables()
    }

    private fun recalculateTables() {
        tables.clear()
        rounds.forEach { r ->
            r.findPlayers().forEach { p -> if (!tables.containsKey(p)) tables[p] = ResultsSummary.atBeginning()}
            r.findPlayedMatches().forEach { m ->
                m.players().forEach { p ->
                    val stats = tables[p.toString()]!!
                    if (p.identifier() == m.winner().identifier())
                        stats.addOneWin()
                    else
                        stats.addOneLost()
                }
            }
        }
    }

    override fun toJson(): Map<String, Any> {
        val output = basicInfo()
        output["currentRound"] = currentRoundIndex
        output["rounds"] = rounds.map { it.toJson() }.toList()
        output["tables"] = tables.toList().sortedWith(ResultsSummary.BY_POINTS_AND_WINS).map {
            mapOf("playerName" to it.first, "stats" to it.second.toJson())
        }
        return output
    }

    override fun findPlayableMatch(matchId: UUID) = currentRound().findMatch(matchId)

    override fun findPlayableMatches() = currentRound().findPlayableMatches()

    override fun isOver() = currentRoundIndex >= rounds.size

    private fun currentRound() = rounds[currentRoundIndex]
}

class ResultsSummary private constructor(
        private var played: Int = 0,
        private var won: Int = 0,
        private var lost: Int = 0,
        private var points: Int = 0,
        private var bestStreak: Int = 0,
        private var currentStreak: Int = 0
){
    companion object {
        const val POINTS_PER_WIN = 2
        private val BY_POINTS: Comparator<Pair<String, ResultsSummary>> = compareByDescending { it.second.points }
        val BY_POINTS_AND_WINS = BY_POINTS.thenBy(compareByDescending { it.second.won }) { it }
        fun atBeginning() = ResultsSummary ()
    }

    fun toJson(): Map<String, Any> {
        return mapOf(
                "played" to played,
                "won" to won,
                "lost" to lost,
                "points" to points,
                "bestStreak" to bestStreak
        )
    }

    fun addOneWin() {
        played++
        won++
        currentStreak++
        points += POINTS_PER_WIN
        if (currentStreak > bestStreak)
            bestStreak = currentStreak
    }

    fun addOneLost() {
        played++
        lost++
        currentStreak = 0
    }
}


