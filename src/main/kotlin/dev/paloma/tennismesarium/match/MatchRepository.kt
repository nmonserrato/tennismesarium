package dev.paloma.tennismesarium.match

interface MatchRepository {
    fun storeMatchCompletedEvent()
}

class InMemoryMatchRepository : MatchRepository {
    private val matches: MutableList<Match> = ArrayList()

    override fun storeMatchCompletedEvent() {
        //TODO("Implement shit")
    }
}
