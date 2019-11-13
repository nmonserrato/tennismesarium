package dev.paloma.tennismesarium.match

import dev.paloma.tennismesarium.match.complete.MatchCompletedEvent

interface MatchRepository {
    fun storeMatchCompletedEvent(event: MatchCompletedEvent)
}

class InMemoryMatchRepository : MatchRepository {
    private val matches: MutableList<Match> = ArrayList()

    override fun storeMatchCompletedEvent(event: MatchCompletedEvent) {
        matches.add(event.match)
    }
}
