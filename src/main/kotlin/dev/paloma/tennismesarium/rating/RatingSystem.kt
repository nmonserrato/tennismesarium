package dev.paloma.tennismesarium.rating

import dev.paloma.tennismesarium.match.Match

interface RatingSystem {
    fun calculateRatings(matches: List<Match>)
}

