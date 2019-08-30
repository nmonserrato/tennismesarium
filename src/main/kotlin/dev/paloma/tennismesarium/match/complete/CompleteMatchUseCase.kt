package dev.paloma.tennismesarium.match.complete

import dev.paloma.tennismesarium.tournament.Tournament
import dev.paloma.tennismesarium.tournament.TournamentRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Service
import java.util.UUID

sealed class CompleteMatchUseCaseResult
object CompleteMatchUseCaseSuccess : CompleteMatchUseCaseResult()
data class CompleteMatchUseCaseValidationError(val message: String) : CompleteMatchUseCaseResult()

@Service
class CompleteMatchUseCase(
    private val eventPublisher: ApplicationEventPublisher,
    private val tournamentRepository: TournamentRepository) {
    fun execute(matchId: UUID, tournamentId: UUID, winnerId: UUID): CompleteMatchUseCaseResult {
        val tournament = tournamentRepository.find(tournamentId)
            ?: return CompleteMatchUseCaseValidationError("Tournament $tournamentId not found")

        val match = tournament.findPlayableMatch(matchId)
            ?: return CompleteMatchUseCaseValidationError(
                "Match $matchId not found in tournament $tournamentId or cannot be played")

        val player = match.players().find { it.identifier() == winnerId }
            ?: return CompleteMatchUseCaseValidationError(
                "Player $winnerId is not playing match $matchId")

        eventPublisher.publishEvent(MatchCompletedEvent(match, tournament, player))
        return CompleteMatchUseCaseSuccess
    }

    @EventListener
    @Order(0)
    fun updateTournamentAfterMatchCompleted(event: MatchCompletedEvent) {
        Tournament.replayEvent(event)
        tournamentRepository.store(event.tournament)
    }
}
