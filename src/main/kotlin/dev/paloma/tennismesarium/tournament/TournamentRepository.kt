package dev.paloma.tennismesarium.tournament

import org.springframework.stereotype.Repository
import java.util.*
import kotlin.collections.HashMap

interface TournamentRepository {
    fun find(identifier: UUID): Tournament?
    fun store(tournament: Tournament)
}

@Repository
class InMemoryTournamentRepository : TournamentRepository {
    private val storage = HashMap<UUID, Tournament>()

    override fun find(identifier: UUID): Tournament? = storage[identifier]

    override fun store(tournament: Tournament) {
        storage[tournament.identifier()] = tournament
    }
}