package dev.paloma.tennismesarium.player

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.io.File
import java.sql.ResultSet
import java.util.*
import javax.sql.DataSource
import kotlin.collections.HashMap


interface PlayersRepository {
    fun createAll(names: List<String>): List<Player>
}

class InMemoryPlayersRepository : PlayersRepository {
    override fun createAll(names: List<String>): List<Player> = names.map { Player(UUID.randomUUID(), it) }
}

class FilePlayersRepository : PlayersRepository {
    private val storageFile = File("database/players.json")
    private val playersByName = HashMap<String, Player>(100)
    private val mapper = ObjectMapper().configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true)

    init {
        storageFile.parentFile.mkdirs()
        storageFile.createNewFile()

        if (storageFile.length() > 0) {
            mapper.readValue<List<Map<String, Any>>>(storageFile)
                    .map { Pair(it["name"] as String, Player.fromJSON(it)) }
                    .forEach {
                        playersByName[it.first] = it.second
                    }
        }
    }

    override fun createAll(names: List<String>): List<Player> {
        return names
                .map { playersByName.getOrPut(it, { Player(UUID.randomUUID(), it) }) }
                .also { persistAll() }
    }

    private fun persistAll() {
        storageFile.writeText(mapper.writeValueAsString(playersByName.values.map { it.toJson() }))
    }
}

class PostgresPlayersRepository private constructor(private val jdbc: NamedParameterJdbcTemplate) : PlayersRepository {
    constructor(dataSource: DataSource) : this(NamedParameterJdbcTemplate(dataSource))

    override fun createAll(names: List<String>): List<Player> {
        val array = names
                .map { mapOf("id" to UUID.randomUUID().toString(), "name" to it) }
                .toTypedArray()

        val insertStmt = "INSERT INTO public.players (id,name) VALUES ( uuid(:id), :name) ON CONFLICT DO NOTHING"
        jdbc.batchUpdate(insertStmt, array)

        val readStmt = "SELECT * FROM public.players WHERE name in (:names)"
        val parameters = MapSqlParameterSource()
        parameters.addValue("names", names)
        return jdbc.query(readStmt, parameters, PlayerRowMapper)
    }

}

object PlayerRowMapper : RowMapper<Player> {
    override fun mapRow(rs: ResultSet, i: Int): Player {
        val id = UUID.fromString(rs.getString(1))
        val name = rs.getString(2)
        return Player(id, name)
    }

}