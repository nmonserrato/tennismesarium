package dev.paloma.tennismesarium.match

import dev.paloma.tennismesarium.player.PlayersRepository
import dev.paloma.tennismesarium.rating.RatingSystem
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

    @Autowired
    private lateinit var playersRepository: PlayersRepository

    @Autowired
    private lateinit var ratingSystem: RatingSystem

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

        ratingSystem.updateRatingsAfterMatch(match.result())

        logger.info("Match {} played and tournament {} updated", matchId, request.tournamentId)
        return ResponseEntity.accepted().build()
    }

    @PostMapping("played")
    fun submitPlayedMatch(
            @RequestBody @Validated request: CreateCompletedMatchRequest
    ): ResponseEntity<Unit> {
        logger.info("Submitted a played match {}", request)

        val players = playersRepository.findAll().map { Pair(it.identifier(), it) }.toMap()

        val match = Match.between(
                players[request.player1] ?: throw IllegalArgumentException("player " + request.player1 + " does not exist"),
                players[request.player2] ?: throw IllegalArgumentException("player " + request.player2 + " does not exist")
        )
        match.complete(request.winnerId)
        matchResultsRepository.storeMatchResult(match)

        ratingSystem.updateRatingsAfterMatch(match.result())

        logger.info("Match {} saved and rankings updated", match.toString())
        return ResponseEntity.noContent().build()
    }
}

data class MatchCompletionRequest(
        @NotNull val tournamentId: UUID,
        @NotNull val winnerId: UUID
)

data class CreateCompletedMatchRequest(
        @NotNull val player1: UUID,
        @NotNull val player2: UUID,
        @NotNull val winnerId: UUID
)
