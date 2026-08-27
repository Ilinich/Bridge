package com.begoml.bridge.core.domain.model

/**
 * The round a season opens on.
 *
 * The first round that still has a fixture ahead of it, so mid-season the calendar opens on what
 * is about to be played rather than on August. Once the season is over every fixture is in the
 * past and the last round is the right answer, not the first.
 */
fun Season.roundAt(nowMillis: Long): SeasonRound? {
    val upcoming = rounds.firstOrNull { round ->
        round.matches.any { it.kickoff.toEpochMilliseconds() >= nowMillis }
    }
    return upcoming ?: rounds.lastOrNull()
}
