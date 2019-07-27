package dev.paloma.tennismesarium.tournament

import dev.paloma.tennismesarium.match.Match
import dev.paloma.tennismesarium.player.PlayersRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@RestController
@RequestMapping("/api/tournament")
class TournamentInformationController {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    private lateinit var playersRepository: PlayersRepository

    @Autowired
    private lateinit var tournamentRepository: TournamentRepository

    @PostMapping
    fun createTournament(@RequestBody @Validated request: TournamentCreationRequest): UUID {
        logger.info("Requested to create tournament with name {} and players {}",
                request.tournamentName, request.playerNames)
        val players = playersRepository.createAll(request.playerNames)
        val tournament = Tournament.createSingleElimination(request.tournamentName, players)
        tournamentRepository.store(tournament)
        return tournament.identifier()
    }

    @GetMapping("{tournamentId}/brackets")
    fun getBrackets(@PathVariable("tournamentId") tournamentId: UUID): ResponseEntity<String> {
        logger.info("Requested brackets for tournament {}", tournamentId)
        val tournament = tournamentRepository.find(tournamentId) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(tournament.printBrackets())
    }

    @GetMapping("{tournamentId}/availableMatches")
    fun availableMatches(@PathVariable("tournamentId") tournamentId: UUID): ResponseEntity<List<Any>> {
        logger.info("Requested available matches for tournament {}", tournamentId)
        val tournament = tournamentRepository.find(tournamentId) ?: return ResponseEntity.notFound().build()
        val availableMatches = tournament.findNextPlayableMatches().map(Match::basicInfo)
        return ResponseEntity.ok(availableMatches)
    }
}

data class TournamentCreationRequest(
        @NotNull val tournamentName: String,
        @NotNull @Size(min = 1) val playerNames: List<String>
)