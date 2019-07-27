package dev.paloma.tennismesarium.match

import dev.paloma.tennismesarium.player.Player

sealed class Match(
        private val games: List<Game>?
) {
    companion object {
        fun between(player1: Player, player2: Player): Match {
            return SinglesMatch.between(player1, player2)
        }
    }
}

class SinglesMatch(
        private val players: Pair<Player, Player>,
        private val games: List<Game>
) : Match(games) {
    companion object {
        fun between(player1: Player, player2: Player): SinglesMatch {
            return SinglesMatch(Pair(player1, player2), emptyList())
        }
    }

    override fun toString(): String {
        return "${players.first} - ${players.second}"
    }
}

class DoublesMatch(
        private val teams: Pair<Team, Team>,
        private val games: List<Game>
) : Match(games) {
}