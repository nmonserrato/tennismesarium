package dev.paloma.tennismesarium.player

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.stereotype.Repository
import java.io.File
import java.util.*
import kotlin.collections.HashMap

interface PlayersRepository {
    fun createAll(names: List<String>): List<Player>
}

class InMemoryPlayersRepository : PlayersRepository {
    override fun createAll(names: List<String>): List<Player> = names.map { Player(UUID.randomUUID(), it) }
}

@Repository
class FilePlayersRepository : PlayersRepository {
    private val storageFile = File("database/players.json")
    private val playersByName = HashMap<String, Player>(100)
    private val mapper = ObjectMapper().configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true)

    init {
        storageFile.parentFile.mkdirs()
        storageFile.createNewFile()

        if(storageFile.length() > 0){
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
