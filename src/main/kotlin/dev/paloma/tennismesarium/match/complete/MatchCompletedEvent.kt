package dev.paloma.tennismesarium.match.complete

import dev.paloma.tennismesarium.match.Match
import dev.paloma.tennismesarium.player.Player
import dev.paloma.tennismesarium.tournament.Tournament
import org.springframework.context.ApplicationEvent

data class MatchCompletedEvent(val match: Match, val tournament: Tournament, val winner: Player)
    : ApplicationEvent(CompleteMatchUseCase::class) {

}
