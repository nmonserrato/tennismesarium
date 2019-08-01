package dev.paloma.tennismesarium.match

import org.springframework.stereotype.Repository
import java.util.*
import kotlin.collections.HashMap

interface MatchRepository {
}

@Repository
class InMemoryMatchRepository : MatchRepository {
    private val storage = HashMap<UUID, Match>()

}