package dev.paloma.tennismesarium.match.complete

import java.util.UUID

sealed class CompleteMatchUseCaseResult
object CompleteMatchUseCaseSuccess : CompleteMatchUseCaseResult()
data class CompleteMatchUseCaseValidationError(val message: String) : CompleteMatchUseCaseResult()

class CompleteMatchUseCase {
    fun execute(matchId: UUID, tournamentId: UUID, winnerId: UUID) : CompleteMatchUseCaseResult {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
