package dev.paloma.tennismesarium.tournament

import dev.paloma.tennismesarium.player.PlayersRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.util.*
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@RestController
@RequestMapping("/api/tournament")
class TournamentController {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    private lateinit var playersRepository: PlayersRepository

    @Autowired
    private lateinit var tournamentRepository: TournamentRepository

    @PostMapping
    fun createTournament(@RequestBody @Validated request: TournamentCreationRequest): ResponseEntity<Unit> {
        logger.info("Requested to create tournament with name {} and players {}",
                request.tournamentName, request.playerNames)
        val players = playersRepository.createAll(request.playerNames)
        val tournament = Tournament.createSingleElimination(request.tournamentName, players)
        tournamentRepository.store(tournament)
        return ResponseEntity.created(
                URI.create("http://localhost:8080/api/tournament/${tournament.identifier()}")
        ).build()
    }

    @GetMapping("{tournamentId}")
    fun getDetails(@PathVariable("tournamentId") tournamentId: UUID): ResponseEntity<Map<String, Any>> {
        logger.info("Requested brackets for tournament {}", tournamentId)
        val tournament = tournamentRepository.find(tournamentId) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(tournament.toJson())
    }
}

data class TournamentCreationRequest(
        @NotNull val tournamentName: String,
        @NotNull @Size(min = 1) val playerNames: List<String>
)