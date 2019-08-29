package dev.paloma.tennismesarium.match

import dev.paloma.tennismesarium.match.complete.CompleteMatchUseCase
import dev.paloma.tennismesarium.match.complete.CompleteMatchUseCaseSuccess
import dev.paloma.tennismesarium.match.complete.CompleteMatchUseCaseValidationError
import org.springframework.http.ResponseEntity
import java.util.UUID

class MatchController(private val completeMatchUseCase: CompleteMatchUseCase) {
    fun submitMatch(matchId: UUID, request: MatchCompletionRequest): ResponseEntity<Any> {
        val response = completeMatchUseCase.execute(matchId, request.tournamentId, request.winnerId)
        return when (response) {
            is CompleteMatchUseCaseSuccess -> ResponseEntity.accepted().build()
            is CompleteMatchUseCaseValidationError -> ResponseEntity.badRequest().body(response.message)
        }
    }
}
