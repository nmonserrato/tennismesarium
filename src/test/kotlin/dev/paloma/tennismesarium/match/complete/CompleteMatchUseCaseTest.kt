package dev.paloma.tennismesarium.match.complete

import dev.paloma.tennismesarium.player.Player
import dev.paloma.tennismesarium.tournament.Tournament
import dev.paloma.tennismesarium.tournament.TournamentRepository
import io.kotlintest.matchers.beOfType
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.specs.BehaviorSpec
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.springframework.context.ApplicationEventPublisher
import java.util.UUID

internal class CompleteMatchUseCaseTest : BehaviorSpec({
    val eventPublisher = mockk<ApplicationEventPublisher>()
    val tournamentRepository = mockk<TournamentRepository>()
    val useCase = CompleteMatchUseCase(eventPublisher, tournamentRepository)

    Given("A tournament with playable matches already exists") {
        val tournamentId = UUID.randomUUID()
        val matchId = UUID.randomUUID()
        val winnerId = UUID.randomUUID()

        When("the use case is invoked with a valid request") {
            every { tournamentRepository.find(tournamentId) } returns mockk()
            every { eventPublisher.publishEvent(any()) } just Runs
            val result = useCase.execute(matchId, tournamentId, winnerId)

            Then("an event is published") {
                verify { eventPublisher.publishEvent(MatchCompletedEvent(matchId, tournamentId, winnerId)) }
            }
            and("success is returned") {
                result shouldBe CompleteMatchUseCaseSuccess
            }
        }

        When("the use case is invoked with a non existing tournament") {
            every { tournamentRepository.find(tournamentId) } returns null

            val result = useCase.execute(matchId, tournamentId, winnerId)

            Then("a failure is returned") {
                result should beOfType<CompleteMatchUseCaseValidationError>()
            }
        }
    }
})

