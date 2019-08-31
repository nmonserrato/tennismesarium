package dev.paloma.tennismesarium.match

import dev.paloma.tennismesarium.match.complete.CompleteMatchUseCase
import dev.paloma.tennismesarium.match.complete.CompleteMatchUseCaseSuccess
import dev.paloma.tennismesarium.match.complete.CompleteMatchUseCaseValidationError
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v2/match")
class MatchController(private val completeMatchUseCase: CompleteMatchUseCase) {

    @PutMapping("{matchId}")
    fun submitMatch(
        @PathVariable("matchId") matchId: UUID,
        @RequestBody @Validated request: MatchCompletionRequest
    ): ResponseEntity<Any> {
        val response = completeMatchUseCase.execute(matchId, request.tournamentId, request.winnerId)
        return when (response) {
            is CompleteMatchUseCaseSuccess -> ResponseEntity.accepted().build()
            is CompleteMatchUseCaseValidationError -> ResponseEntity.badRequest().body(response.message)
        }
    }
}
