package com.begoml.bridge.core.data.repository

import com.begoml.bridge.core.data.TeamNames
import com.begoml.bridge.core.data.model.Loadable
import com.begoml.bridge.core.data.model.map
import com.begoml.bridge.core.data.model.Match
import com.begoml.bridge.core.data.model.Season
import com.begoml.bridge.core.data.model.SeasonRound
import com.begoml.bridge.core.data.model.toMatch
import com.begoml.bridge.core.data.model.toSeason
import com.begoml.bridge.core.data.openfootball.SeasonApi
import com.begoml.bridge.core.data.sportsdb.SportsDbApi
import com.begoml.bridge.foundation.cache.InMemoryCache
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Fixtures from both sources, each on the time to live its nature deserves.
 *
 * The next match sits under a countdown, so it goes stale in a minute; the season is a static file
 * that gains scores as rounds are played, so it holds for hours. The two are never merged: they
 * answer different questions and fail independently.
 */
class MatchRepository internal constructor(
    private val teamId: String,
    private val clubName: String,
    sportsDb: SportsDbApi,
    seasonApi: SeasonApi,
    dispatcher: CoroutineDispatcher,
    backgroundScope: CoroutineScope,
    nowMillis: () -> Long,
) {

    private val nextMatchCache = InMemoryCache<String, List<Match>>(
        loader = { id -> sportsDb.nextEvents(id).mapNotNull { it.toMatch() } },
        dispatcher = dispatcher,
        nowMillis = nowMillis,
        staleAfter = 60.seconds,
        expireAfter = 10.minutes,
        backgroundScope = backgroundScope,
    )

    private val lastResultCache = InMemoryCache<String, List<Match>>(
        loader = { id -> sportsDb.lastEvents(id).mapNotNull { it.toMatch() } },
        dispatcher = dispatcher,
        nowMillis = nowMillis,
        staleAfter = 5.minutes,
        expireAfter = 1.hours,
        backgroundScope = backgroundScope,
    )

    private val seasonCache = InMemoryCache<String, Season>(
        loader = { seasonApi.season().toSeason() },
        dispatcher = dispatcher,
        nowMillis = nowMillis,
        staleAfter = 6.hours,
        expireAfter = 24.hours,
        backgroundScope = backgroundScope,
    )

    fun nextMatch(): Flow<Loadable<Match?>> =
        nextMatchCache.loadable(teamId).map { loadable ->
            loadable.map { matches -> matches.minByOrNull { it.kickoff } }
        }

    fun lastResult(): Flow<Loadable<Match?>> =
        lastResultCache.loadable(teamId).map { loadable ->
            loadable.map { matches -> matches.maxByOrNull { it.kickoff } }
        }

    fun season(): Flow<Loadable<Season>> = seasonCache.loadable(SeasonKey)

    /** True when either side of the fixture is the club this app follows. */
    fun isOurs(homeName: String, awayName: String): Boolean =
        TeamNames.matches(homeName, clubName) || TeamNames.matches(awayName, clubName)

    fun currentRound(season: Season, nowMillis: Long): SeasonRound? {
        val upcoming = season.rounds.firstOrNull { round ->
            round.matches.any { it.kickoff.toEpochMilliseconds() >= nowMillis }
        }
        return upcoming ?: season.rounds.lastOrNull()
    }

    private companion object {
        const val SeasonKey = "en.1"
    }
}
