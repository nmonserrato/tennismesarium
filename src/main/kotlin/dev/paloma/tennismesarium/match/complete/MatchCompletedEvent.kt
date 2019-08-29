package dev.paloma.tennismesarium.match.complete

import org.springframework.context.ApplicationEvent
import java.util.UUID

data class MatchCompletedEvent(val matchId: UUID, val tournamentId: UUID, val winnerId: UUID)
    : ApplicationEvent(CompleteMatchUseCase::class) {

}
