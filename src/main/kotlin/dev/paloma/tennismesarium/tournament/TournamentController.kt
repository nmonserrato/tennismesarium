package dev.paloma.tennismesarium.tournament

import dev.paloma.tennismesarium.player.Player
import dev.paloma.tennismesarium.player.Player.Companion.BY_NAME
import dev.paloma.tennismesarium.player.PlayersRepository
import dev.paloma.tennismesarium.tournament.SingleEliminationTournament.Companion.DESCENDING_CREATION_DATE
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
        logger.info("Requested to create tournament of type {} with name {} and players {}",
                request.mode, request.tournamentName, request.playerNames)
        val players = playersRepository.createAll(request.playerNames)

        val tournament = if(request.mode == "elimination")
            Tournament.createSingleElimination(request.tournamentName, players)
        else
            Tournament.createFixtures(request.tournamentName, players)

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

    @DeleteMapping("{tournamentId}")
    fun deleteTournament(@PathVariable("tournamentId") tournamentId: UUID): ResponseEntity<Unit> {
        logger.info("Requested to delete tournament {}", tournamentId)
        tournamentRepository.delete(tournamentId)
        return ResponseEntity.accepted().build()
    }

    @GetMapping("list")
    fun getDetails(): ResponseEntity<List<Map<String, Any>>> {
        logger.info("Requested list of tournaments")
        val tournaments = tournamentRepository
                .findAll()
                .sortedWith(DESCENDING_CREATION_DATE)
                .map(Tournament::basicInfo)

        return ResponseEntity.ok(tournaments)
    }
}

data class TournamentCreationRequest(
        @NotNull val tournamentName: String,
        @NotNull val mode: String,
        @NotNull @Size(min = 1) val playerNames: List<String>
)