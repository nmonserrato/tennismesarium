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
        val tournament = aValidTournament()
        val tournamentId = tournament.identifier()
        every { tournamentRepository.find(tournamentId) } returns tournament

        val match = tournament.findPlayableMatches()[0]
        val matchId = match.identifier()

        val winner = match.players()[0]
        val winnerId = winner.identifier()

        When("the use case is invoked with a non existing tournament") {
            every { tournamentRepository.find(tournamentId) } returns null

            val result = useCase.execute(matchId, tournamentId, winnerId)

            Then("a failure is returned") {
                result should beOfType<CompleteMatchUseCaseValidationError>()
                (result as CompleteMatchUseCaseValidationError).message shouldBe  "Tournament $tournamentId not found"
            }
        }

        When("the use case is invoked with a match that is not part of the tournament") {
            every { tournamentRepository.find(tournamentId) } returns tournament
            val invalidMatchId = UUID.randomUUID()
            val result = useCase.execute(invalidMatchId, tournamentId, winnerId)

            Then("a failure is returned") {
                result should beOfType<CompleteMatchUseCaseValidationError>()
                (result as CompleteMatchUseCaseValidationError).message shouldBe
                    "Match $invalidMatchId not found in tournament $tournamentId or cannot be played"
            }
        }

        When("the use case is invoked with a winner that is not playing that match") {
            every { tournamentRepository.find(tournamentId) } returns tournament
            val invalidWinnerId = UUID.randomUUID()
            val result = useCase.execute(matchId, tournamentId, invalidWinnerId)

            Then("a failure is returned") {
                result should beOfType<CompleteMatchUseCaseValidationError>()
                (result as CompleteMatchUseCaseValidationError).message shouldBe
                    "Player $invalidWinnerId is not playing match $matchId"
            }
        }

        When("the use case is invoked with a valid request") {
            every { eventPublisher.publishEvent(any()) } just Runs
            val result = useCase.execute(matchId, tournamentId, winnerId)

            Then("an event is published") {
                verify { eventPublisher.publishEvent(MatchCompletedEvent(match, tournament, winner)) }
            }
            and("success is returned") {
                result shouldBe CompleteMatchUseCaseSuccess
            }
        }
    }
})

private fun aValidTournament(): Tournament {
    val players = listOf(
        Player(UUID.randomUUID(), "Player 1"),
        Player(UUID.randomUUID(), "Player 2"))

    return Tournament.createSingleElimination("Test Tournament", players)
}

