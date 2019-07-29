package dev.paloma.tennismesarium.tournament

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.stereotype.Repository
import java.io.File
import java.util.*
import kotlin.collections.HashMap

interface TournamentRepository {
    fun find(identifier: UUID): Tournament?
    fun store(tournament: Tournament)
}

class InMemoryTournamentRepository : TournamentRepository {
    private val storage = HashMap<UUID, Tournament>()

    override fun find(identifier: UUID): Tournament? = storage[identifier]

    override fun store(tournament: Tournament) {
        storage[tournament.identifier()] = tournament
    }
}

@Repository
class FileTournamentRepository : TournamentRepository {
    private val databaseFolder = File("database/")
    private val mapper = ObjectMapper().configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true)

    override fun find(identifier: UUID): Tournament? {
        val jsonFile = tournamentFile(identifier)
        if (!jsonFile.canRead() || jsonFile.length() <= 0)
            return null

        val json = mapper.readValue<Map<String, Any>>(jsonFile)
        return SingleEliminationTournament.fromJson(json)
    }

    override fun store(tournament: Tournament) {
        val jsonFile = tournamentFile(tournament.identifier())
        jsonFile.writeText(mapper.writeValueAsString(tournament.toJson()))
    }

    private fun tournamentFile(identifier: UUID): File {
        return File(databaseFolder, "tournament_$identifier.json")
    }

}