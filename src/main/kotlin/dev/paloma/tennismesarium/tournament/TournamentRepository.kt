package dev.paloma.tennismesarium.tournament

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.io.File
import java.sql.ResultSet
import java.util.*
import javax.sql.DataSource
import kotlin.collections.HashMap

interface TournamentRepository {
    fun find(identifier: UUID): Tournament?
    fun store(tournament: Tournament)
    fun findAll(): List<Tournament>
    fun delete(tournamentId: UUID)
}

class InMemoryTournamentRepository : TournamentRepository {
    private val storage = HashMap<UUID, Tournament>()

    override fun find(identifier: UUID): Tournament? = storage[identifier]

    override fun findAll(): List<Tournament> {
        return storage.values.toList()
    }

    override fun store(tournament: Tournament) {
        storage[tournament.identifier()] = tournament
    }

    override fun delete(tournamentId: UUID) {
        storage.remove(tournamentId)
    }
}

class FileTournamentRepository : TournamentRepository {
    private val databaseFolder = File("database/")
    private val mapper = ObjectMapper().configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true)

    init {
        databaseFolder.mkdirs()
    }

    override fun find(identifier: UUID): Tournament? {
        val jsonFile = tournamentFile(identifier)
        if (!jsonFile.canRead() || jsonFile.length() <= 0)
            return null

        val json = mapper.readValue<Map<String, Any>>(jsonFile)
        return SingleEliminationTournament.fromJson(json)
    }

    override fun findAll(): List<Tournament> {
        return databaseFolder
                .listFiles { _, filename -> filename.matches(Regex("tournament.*.json")) }
                .map { it.name.replace("tournament_", "").replace(".json", "") }
                .map (UUID::fromString)
                .map { find(it) !! }
    }

    override fun store(tournament: Tournament) {
        val jsonFile = tournamentFile(tournament.identifier())
        jsonFile.writeText(mapper.writeValueAsString(tournament.toJson()))
    }

    override fun delete(tournamentId: UUID) {
        val file = tournamentFile(tournamentId)
        if (file.exists()) file.delete()
    }

    private fun tournamentFile(identifier: UUID): File {
        return File(databaseFolder, "tournament_$identifier.json")
    }

}

class PostgresTournamentRepository private constructor(private val jdbc: NamedParameterJdbcTemplate) : TournamentRepository {
    private val mapper = ObjectMapper()

    constructor(dataSource: DataSource) : this(NamedParameterJdbcTemplate(dataSource))

    override fun store(tournament: Tournament) {
        val serialized = mapper.writeValueAsString(tournament.toJson())
        jdbc.update(""" INSERT INTO public.tournaments (id, definition) VALUES (uuid(:id), json(:json))
                                ON CONFLICT (id) DO UPDATE 
                                SET definition = excluded.definition """,
                mapOf(
                        "id" to tournament.identifier().toString(),
                        "json" to serialized))
    }

    override fun findAll(): List<Tournament> {
        return jdbc.query("SELECT * from public.tournaments", TournamentRowMapper)
    }

    override fun delete(tournamentId: UUID) {
        jdbc.update("DELETE FROM public.tournaments WHERE id=uuid(:id)", mapOf("id" to tournamentId.toString()))
    }

    override fun find(identifier: UUID): Tournament? {
        return try {
            jdbc.queryForObject(
                    "SELECT * from public.tournaments where id=uuid(:id)",
                    mapOf("id" to identifier.toString()),
                    TournamentRowMapper)
        } catch (e: EmptyResultDataAccessException) {
            null
        }
    }

}

object TournamentRowMapper : RowMapper<Tournament> {
    private val mapper = ObjectMapper()

    override fun mapRow(rs: ResultSet, idx: Int): Tournament? {
        val content = rs.getString("definition")
        val json = mapper.readValue<Map<String, Any>>(content)
        return SingleEliminationTournament.fromJson(json)

    }
}