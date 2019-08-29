package dev.paloma.tennismesarium.match.complete

import dev.paloma.tennismesarium.tournament.TournamentRepository
import org.springframework.context.ApplicationEventPublisher
import java.util.UUID

sealed class CompleteMatchUseCaseResult
object CompleteMatchUseCaseSuccess : CompleteMatchUseCaseResult()
data class CompleteMatchUseCaseValidationError(val message: String) : CompleteMatchUseCaseResult()

class CompleteMatchUseCase(
    private val eventPublisher: ApplicationEventPublisher,
    private val tournamentRepository: TournamentRepository) {
    fun execute(matchId: UUID, tournamentId: UUID, winnerId: UUID) : CompleteMatchUseCaseResult {
        tournamentRepository.find(tournamentId)
            ?: return CompleteMatchUseCaseValidationError("Tournament $tournamentId not found")
        eventPublisher.publishEvent(MatchCompletedEvent(matchId, tournamentId, winnerId))
        return CompleteMatchUseCaseSuccess
    }
}
