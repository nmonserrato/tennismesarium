package dev.paloma.tennismesarium.match

import dev.paloma.tennismesarium.match.complete.CompleteMatchUseCase
import dev.paloma.tennismesarium.match.complete.CompleteMatchUseCaseSuccess
import dev.paloma.tennismesarium.match.complete.CompleteMatchUseCaseValidationError
import io.kotlintest.specs.BehaviorSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.util.UUID

internal class MatchControllerTest : BehaviorSpec({
    val completeMatchUseCase = mockk<CompleteMatchUseCase>()
    val controller = MatchController(completeMatchUseCase)

    Given("A tournament with playable matches already exists") {
        val tournamentId = UUID.randomUUID()
        val matchId = UUID.randomUUID()
        val winnerId = UUID.randomUUID()
        When("the result for a match is submitted with a valid request") {
            val request = MatchCompletionRequest(tournamentId, winnerId)

            every { completeMatchUseCase.execute(any(), any(), any()) } returns CompleteMatchUseCaseSuccess

            val response: ResponseEntity<Any> = controller.submitMatch(matchId, request)

            Then("use case for match completed is invoked") {
                verify { completeMatchUseCase.execute(matchId, tournamentId, winnerId) }
            }
            and("the result is accepted") {
                assertThat(response.statusCode).isEqualTo(HttpStatus.ACCEPTED)
            }
        }

        When("an invalid request to complete a match is submitted") {
            val request = MatchCompletionRequest(tournamentId, winnerId)

            every {
                completeMatchUseCase.execute(any(), any(), any())
            } returns CompleteMatchUseCaseValidationError("Match is already completed!")

            val response: ResponseEntity<Any> = controller.submitMatch(matchId, request)

            Then("use case for match completed is invoked") {
                verify { completeMatchUseCase.execute(matchId, tournamentId, winnerId) }
            }
            and("the result is bad request") {
                assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
                assertThat(response.body).isEqualTo("Match is already completed!")
            }
        }

        When("the match completions is processed with an unexpected error") {
            val request = MatchCompletionRequest(tournamentId, winnerId)

            every { completeMatchUseCase.execute(matchId, tournamentId, winnerId) } throws NullPointerException()

            Then("the error is rethrown") {
                assertThatExceptionOfType(NullPointerException::class.java).isThrownBy {
                    controller.submitMatch(matchId, request)
                }
            }
        }
    }
})
