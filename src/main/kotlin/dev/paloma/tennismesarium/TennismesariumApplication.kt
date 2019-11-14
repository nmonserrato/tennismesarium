package dev.paloma.tennismesarium

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.paloma.tennismesarium.match.InMemoryMatchResultsRepository
import dev.paloma.tennismesarium.match.MatchResultsRepository
import dev.paloma.tennismesarium.match.PostgresMatchResultsRepository
import dev.paloma.tennismesarium.player.InMemoryPlayersRepository
import dev.paloma.tennismesarium.player.PostgresPlayersRepository
import dev.paloma.tennismesarium.tournament.InMemoryTournamentRepository
import dev.paloma.tennismesarium.tournament.PostgresTournamentRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.URISyntaxException
import java.util.*
import javax.sql.DataSource


@SpringBootApplication
@EnableAutoConfiguration(exclude = [
	JdbcTemplateAutoConfiguration::class,
	DataSourceTransactionManagerAutoConfiguration::class
])
class TennismesariumApplication

fun main(args: Array<String>) {
	runApplication<TennismesariumApplication>(*args)
}

@Configuration
class MainConfig {
	@Bean
	@Throws(URISyntaxException::class)
	fun dataSource(): DataSource? {
		val config = HikariConfig()
		config.jdbcUrl = System.getenv("JDBC_DATABASE_URL")
		config.username = System.getenv("JDBC_DATABASE_USERNAME")
		config.password = System.getenv("JDBC_DATABASE_PASSWORD")
		config.addDataSourceProperty("cachePrepStmts", "true")
		config.addDataSourceProperty("prepStmtCacheSize", "250")
		config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")

		if (config.jdbcUrl == null)
			return null

		return HikariDataSource(config)
	}

	@Bean
	fun tournamentRepository(@Autowired dataSource: DataSource?) =
		if (dataSource == null)
			InMemoryTournamentRepository()
		else
			PostgresTournamentRepository(dataSource)

	@Bean
	fun playersRepository(@Autowired dataSource: DataSource?) =
		if (dataSource == null)
			InMemoryPlayersRepository()
		else
			PostgresPlayersRepository(dataSource)

	@Bean
	fun matchResultsRepository(@Autowired dataSource: DataSource?) =
			Optional.ofNullable(dataSource)
					.map { PostgresMatchResultsRepository(it) as MatchResultsRepository }
					.orElse(InMemoryMatchResultsRepository())
}