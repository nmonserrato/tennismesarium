package dev.paloma.tennismesarium.match

import dev.paloma.tennismesarium.rating.MatchResult
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import java.util.*
import javax.sql.DataSource
import kotlin.collections.ArrayList

interface MatchResultsRepository {
    fun storeMatchResult(match: Match)
    fun loadAllResults(): List<MatchResult>
}

class InMemoryMatchResultsRepository : MatchResultsRepository {
    private val matches: MutableList<MatchResult> = ArrayList()

    override fun storeMatchResult(match: Match) {
        matches.add(
                MatchResult(
                        match.players()[0].identifier(),
                        match.players()[1].identifier(),
                        match.winner().identifier())
        )
    }

    override fun loadAllResults(): List<MatchResult> {
        return matches;
    }
}

class PostgresMatchResultsRepository private constructor(private val jdbc: NamedParameterJdbcTemplate) : MatchResultsRepository {
    constructor(dataSource: DataSource) : this(NamedParameterJdbcTemplate(dataSource))

    override fun storeMatchResult(match: Match) {
        val insertStmt = "INSERT INTO public.matchresults (player1, player2, winner) " +
                "VALUES ( uuid(:p1), uuid(:p2), uuid(:w))"

        jdbc.update(insertStmt, mapOf(
                "p1" to match.players()[0].identifier().toString(),
                "p2" to match.players()[1].identifier().toString(),
                "w" to match.winner().identifier().toString())
        )
    }

    override fun loadAllResults(): List<MatchResult> {
        val readStmt = "SELECT player1, player2, winner FROM public.matchresults ORDER BY created"
        return jdbc.query(readStmt, MatchResultRowMapper)
    }
}

object MatchResultRowMapper : RowMapper<MatchResult> {
    override fun mapRow(rs: ResultSet, i: Int): MatchResult {
        val p1 = UUID.fromString(rs.getString(1))
        val p2 = UUID.fromString(rs.getString(2))
        val w = UUID.fromString(rs.getString(3))
        return MatchResult(p1, p2, w)
    }
}