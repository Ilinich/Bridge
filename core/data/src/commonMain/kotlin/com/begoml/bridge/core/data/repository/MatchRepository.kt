package com.begoml.bridge.core.data.repository

import com.begoml.bridge.core.data.TeamNames
import com.begoml.bridge.core.data.db.SeasonDao
import com.begoml.bridge.core.data.db.Syncer
import com.begoml.bridge.core.data.db.toEntity
import com.begoml.bridge.core.data.db.toSeason
import com.begoml.bridge.core.data.model.Loadable
import com.begoml.bridge.core.data.model.Match
import com.begoml.bridge.core.data.model.Season
import com.begoml.bridge.core.data.model.SeasonMatch
import com.begoml.bridge.core.data.model.SeasonRound
import com.begoml.bridge.core.data.model.map
import com.begoml.bridge.core.data.model.roundAt
import com.begoml.bridge.core.data.model.toMatch
import com.begoml.bridge.core.data.model.toSeason
import com.begoml.bridge.core.data.openfootball.SeasonApi
import com.begoml.bridge.core.data.previousSeasonId
import com.begoml.bridge.core.data.seasonIdAt
import com.begoml.bridge.core.data.sportsdb.SportsDbApi
import com.begoml.bridge.foundation.cache.InMemoryCache
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Fixtures from two sources, each held for as long as its nature allows.
 *
 * The next match and the last result sit under a countdown, so they live in memory with a short
 * time to live and are re-fetched often. A season is the opposite: 380 fixtures that stop changing
 * the moment the last one is played, so they go to disk — a **finished** season is fetched exactly
 * once ever, and the season in progress is refreshed a few times a day as scores land.
 */
class MatchRepository internal constructor(
    private val teamId: String,
    private val clubName: String,
    sportsDb: SportsDbApi,
    private val seasonApi: SeasonApi,
    private val seasonDao: SeasonDao,
    private val syncer: Syncer,
    dispatcher: CoroutineDispatcher,
    private val backgroundScope: CoroutineScope,
    private val nowMillis: () -> Long,
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

    fun nextMatch(): Flow<Loadable<Match?>> =
        nextMatchCache.loadable(teamId).map { loadable ->
            loadable.map { matches -> matches.minByOrNull { it.kickoff } }
        }

    fun lastResult(): Flow<Loadable<Match?>> =
        lastResultCache.loadable(teamId).map { loadable ->
            loadable.map { matches -> matches.maxByOrNull { it.kickoff } }
        }

    /** The season now being played, falling back to the last one while the new one is unpublished. */
    fun season(): Flow<Loadable<Season>> = flow {
        val seasonId = resolveSeasonId()
        backgroundScope.launch { runCatching { cacheFinishedSeason(previousSeasonId(seasonId)) } }

        val stored = seasonDao.observeSeason(seasonId)
            .map { rows -> rows.takeIf { it.isNotEmpty() }?.toSeason(seasonId) }

        emitAll(
            persistedResource(stored = stored) {
                syncer.sync(key = seasonKey(seasonId), ttl = ttlFor(seasonId)) { fetch(seasonId) }
            },
        )
    }

    fun isOurs(homeName: String, awayName: String): Boolean =
        TeamNames.matches(homeName, clubName) || TeamNames.matches(awayName, clubName)

    fun currentRound(season: Season, nowMillis: Long): SeasonRound? = season.roundAt(nowMillis)

    fun match(id: String): Flow<SeasonMatch?> = seasonDao.observeMatch(id).map { entity ->
        entity?.let { listOf(it).toSeason("").rounds.first().matches.first() }
    }

    /**
     * A season the calendar does not show, kept because it never changes again.
     *
     * One request in the lifetime of an install, and it is on disk for good.
     */
    private suspend fun cacheFinishedSeason(seasonId: String) {
        syncer.sync(key = seasonKey(seasonId), ttl = null) { fetch(seasonId) }
    }

    private suspend fun resolveSeasonId(): String {
        val current = seasonIdAt(nowMillis())
        runCatching {
            syncer.sync(key = seasonKey(current), ttl = CurrentSeasonTtl) { fetch(current) }
        }
        return if (seasonDao.count(current) > 0) current else previousSeasonId(current)
    }

    private suspend fun fetch(seasonId: String) {
        val envelope = seasonApi.season(seasonId) ?: return
        val matches = envelope.toSeason(seasonId).rounds
            .flatMap { round -> round.matches }
            .map { match -> match.toEntity(seasonId) }
        if (matches.isNotEmpty()) seasonDao.replaceSeason(seasonId, matches)
    }

    private fun ttlFor(seasonId: String): Duration? =
        if (seasonId == seasonIdAt(nowMillis())) CurrentSeasonTtl else null

    private fun seasonKey(seasonId: String) = "season:$seasonId"

    private companion object {
        val CurrentSeasonTtl = 6.hours
    }
}
