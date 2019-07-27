package dev.paloma.tennismesarium.player

import java.util.*

class Player(
        private val id: UUID,
        private val name: String
) {
    override fun toString(): String {
        return name
    }
}
