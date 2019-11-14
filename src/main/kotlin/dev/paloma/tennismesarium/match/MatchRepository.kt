package dev.paloma.tennismesarium.match

interface MatchRepository {
    fun storeCompletedMatch(match: Match)
}

class InMemoryMatchRepository : MatchRepository {
    private val matches: MutableList<Match> = ArrayList()

    override fun storeCompletedMatch(match: Match) {
        matches.add(match)
    }
}
