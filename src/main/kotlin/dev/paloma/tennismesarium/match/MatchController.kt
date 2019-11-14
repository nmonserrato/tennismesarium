package dev.paloma.tennismesarium.match

import dev.paloma.tennismesarium.tournament.TournamentRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.constraints.NotNull

@RestController
@RequestMapping("/api/match")
class MatchController {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    private lateinit var tournamentRepository: TournamentRepository

    @Autowired
    private lateinit var matchResultsRepository: MatchResultsRepository

    @PutMapping("{matchId}")
    fun completeMatch(
            @PathVariable("matchId") matchId: UUID,
            @RequestBody @Validated request: MatchCompletionRequest
    ): ResponseEntity<Unit> {
        logger.info("Requested to complete match {} of tournament {} with winner {}",
                matchId, request.tournamentId, request.winnerId)

        val tournament = tournamentRepository.find(request.tournamentId)
                ?: return ResponseEntity.notFound().build()

        val match = tournament.findPlayableMatch(matchId)
                ?: throw IllegalArgumentException("No match $matchId found in tournament")

        match.complete(request.winnerId)
        matchResultsRepository.storeMatchResult(match)

        tournament.onMatchCompleted()
        tournamentRepository.store(tournament)

        logger.info("Match {} played and tournament {} updated", matchId, request.tournamentId)
        return ResponseEntity.accepted().build()
    }
}

data class MatchCompletionRequest(
        @NotNull val tournamentId: UUID,
        @NotNull val winnerId: UUID
)
