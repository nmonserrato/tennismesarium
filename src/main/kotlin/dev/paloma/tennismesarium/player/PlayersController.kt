package dev.paloma.tennismesarium.player

import dev.paloma.tennismesarium.match.MatchCompletionRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/players")
class PlayersController {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    private lateinit var playersRepository: PlayersRepository

    @GetMapping("available")
    fun getPlayers(): ResponseEntity<List<Map<String, Any>>> {
        logger.info("Requested list of players")
        val players = playersRepository
                .findAll()
                .sortedWith(Player.BY_NAME)
                .map(Player::toJson)

        return ResponseEntity.ok(players)
    }

    @PostMapping
    fun createPlayers(@RequestBody @Validated request: List<String>): ResponseEntity<Unit> {
        logger.info("Creating all players (if not exist) with names {}", request)
        playersRepository.createAll(request)

        return ResponseEntity.noContent().build()
    }
}
