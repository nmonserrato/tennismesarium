package dev.paloma.tennismesarium

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.URISyntaxException
import javax.sql.DataSource


@SpringBootApplication
class TennismesariumApplication

fun main(args: Array<String>) {
	runApplication<TennismesariumApplication>(*args)
}

@Configuration
class MainConfig {
	@Bean
	@Throws(URISyntaxException::class)
	fun dataSource(): DataSource {
		val config = HikariConfig()
		config.jdbcUrl = System.getenv("JDBC_DATABASE_URL")
		config.username = System.getenv("JDBC_DATABASE_USERNAME")
		config.password = System.getenv("JDBC_DATABASE_PASSWORD")
		config.addDataSourceProperty("cachePrepStmts", "true")
		config.addDataSourceProperty("prepStmtCacheSize", "250")
		config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
		return HikariDataSource(config)
	}
}