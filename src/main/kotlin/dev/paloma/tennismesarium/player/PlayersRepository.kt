package dev.paloma.tennismesarium.player

import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import java.util.*
import javax.sql.DataSource
import kotlin.collections.HashSet
import kotlin.streams.toList


interface PlayersRepository {
    fun createAll(names: List<String>): List<Player>
    fun findAll(): List<Player>
}

class InMemoryPlayersRepository : PlayersRepository {
    val players: MutableSet<Player> = HashSet()

    override fun findAll(): List<Player> {
        return players.toList()
    }

    override fun createAll(names: List<String>): List<Player> =
            names.stream().map { Player(UUID.randomUUID(), it) }.peek { players.add(it) }.toList()
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

    override fun findAll(): List<Player> {
        val readStmt = "SELECT * FROM public.players"
        return jdbc.query(readStmt, PlayerRowMapper)
    }
}

object PlayerRowMapper : RowMapper<Player> {
    override fun mapRow(rs: ResultSet, i: Int): Player {
        val id = UUID.fromString(rs.getString(1))
        val name = rs.getString(2)
        val slackId: String? = rs.getString(3)
        return Player(id, name, slackId)
    }

}