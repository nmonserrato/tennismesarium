package dev.paloma.tennismesarium.match

import dev.paloma.tennismesarium.tournament.TournamentRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*
import javax.validation.constraints.NotNull

@RestController
@RequestMapping("/api/match")
class MatchController {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    private lateinit var tournamentRepository: TournamentRepository

    @PutMapping
    fun completeMatch(@RequestBody @Validated request: MatchCompletionRequest): ResponseEntity<Unit> {
        logger.info("Requested to complete match {} with winner {}", request.matchId, request.winnerId)
        //TODO implement
        return ResponseEntity.accepted().build()
    }
}

data class MatchCompletionRequest(
        @NotNull val matchId: UUID,
        @NotNull val winnerId: UUID
)