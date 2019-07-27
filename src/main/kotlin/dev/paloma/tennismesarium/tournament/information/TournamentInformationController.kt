package dev.paloma.tennismesarium.tournament.information

import dev.paloma.tennismesarium.tournament.Tournament
import org.slf4j.LoggerFactory
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@RestController
@RequestMapping("/api/tournament")
class TournamentInformationController {

    private val logger = LoggerFactory.getLogger(javaClass)

    @PostMapping
    fun createTournament(@RequestBody @Validated request: TournamentCreationRequest): UUID {
        logger.info("Requested to create tournament with name {} and players {}",
                request.tournamentName, request.playerNames)
        val tournament = Tournament.createSingleElimination(request.tournamentName, request.playerNames)
        return tournament.identifier()
    }

    @GetMapping("{tournamentId}/brackets")
    fun getBrackets(@PathVariable("tournamentId") tournamentId: UUID): String {
        logger.info("Requested brackets for tournament {}", tournamentId)
        val tournament = Tournament.createSingleElimination(
                "TomokiCup1",
                listOf("Sergio", "Miquel", "Arjen", "Olivier", "Laia", "Juana", "Nino", "Diego", "Germanno")
        )
        return tournament.printBrackets()
    }

    @GetMapping("{tournamentId}/availableMatches")
    fun availableMatches(@PathVariable("tournamentId") tournamentId: UUID): List<String> {
        logger.info("Requested available matches for tournament {}", tournamentId)
        return emptyList()
    }
}

data class TournamentCreationRequest(
        @NotNull val tournamentName: String,
        @NotNull @Size(min = 1) val playerNames: List<String>
)