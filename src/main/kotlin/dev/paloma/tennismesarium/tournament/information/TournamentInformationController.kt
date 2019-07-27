package dev.paloma.tennismesarium.tournament.information

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/api/tournament")
class TournamentInformationController {

    private val logger = LoggerFactory.getLogger(javaClass)

    @GetMapping("{tournamentId}/availableMatches")
    fun availableMatches(@PathVariable("tournamentId") tournamentId: UUID): List<String> {
        logger.info("Requested available matches for tournament {}", tournamentId)
        return emptyList()
    }
}