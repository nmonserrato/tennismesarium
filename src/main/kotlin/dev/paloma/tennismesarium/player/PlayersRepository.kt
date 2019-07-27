package dev.paloma.tennismesarium.player

import org.springframework.stereotype.Repository
import java.util.*

interface PlayersRepository {
    fun createAll(names: List<String>): List<Player>
}

@Repository
class InMemoryPlayersRepository : PlayersRepository {
    override fun createAll(names: List<String>): List<Player> = names.map { Player(UUID.randomUUID(), it) }
}