package dev.paloma.tennismesarium.player

import java.util.*

class Player(
        private val id: UUID,
        private val name: String,
        val slackId: String? = null
) {
    override fun toString(): String {
        return name
    }

    fun identifier() = id

    fun toJson(): Map<String, String> {
        return mapOf("id" to id.toString(), "name" to name, "slackId" to (slackId ?: ""))
    }

    companion object {
        val BY_NAME = compareBy<Player> { it.name }

        fun fromJSON(json: Map<String, Any>): Player {
            val id = UUID.fromString(json["id"] as String)
            val name = json["name"] as String
            val slackId = json["slackId"] as String?
            return Player(id, name, slackId)
        }
    }
}
