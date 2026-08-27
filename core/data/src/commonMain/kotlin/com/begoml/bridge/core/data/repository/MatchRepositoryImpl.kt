package com.begoml.bridge.core.data.repository

import com.begoml.bridge.foundation.resource.persistedResource
import com.begoml.bridge.foundation.resource.cachedResource
import com.begoml.bridge.core.domain.repository.MatchRepository

import com.begoml.bridge.core.data.db.SeasonDao
import com.begoml.bridge.core.data.db.Syncer
import com.begoml.bridge.core.data.db.toEntity
import com.begoml.bridge.core.data.db.SeasonMatchEntity
import com.begoml.bridge.core.data.db.toSeasonMatch
import com.begoml.bridge.core.data.db.toSeason
import com.begoml.bridge.foundation.resource.Loadable
import com.begoml.bridge.core.domain.model.Match
import com.begoml.bridge.core.domain.model.Season
import com.begoml.bridge.core.domain.model.SeasonMatch
import com.begoml.bridge.core.domain.model.SeasonRound
import com.begoml.bridge.foundation.resource.map
import com.begoml.bridge.core.domain.model.roundAt
import com.begoml.bridge.core.data.mapper.toMatch
import com.begoml.bridge.core.data.mapper.toSeason
import com.begoml.bridge.core.data.openfootball.SeasonApi
import com.begoml.bridge.core.domain.previousSeasonId
import com.begoml.bridge.core.domain.seasonIdAt
import com.begoml.bridge.core.data.sportsdb.SportsDbApi
import com.begoml.bridge.foundation.resource.InMemoryCache
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlin.time.Clock
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

internal class MatchRepositoryImpl(
    sportsDb: SportsDbApi,
    private val seasonApi: SeasonApi,
    private val seasonDao: SeasonDao,
    private val syncer: Syncer,
    private val dispatcher: CoroutineDispatcher,
    private val backgroundScope: CoroutineScope,
    private val clock: Clock,
) : MatchRepository {

    private val nextMatchCache = InMemoryCache<String, List<Match>>(
        loader = { id -> sportsDb.nextEvents(id).mapNotNull { it.toMatch() } },
        dispatcher = dispatcher,
        clock = clock,
        staleAfter = 60.seconds,
        expireAfter = 10.minutes,
        backgroundScope = backgroundScope,
    )

    private val lastResultCache = InMemoryCache<String, List<Match>>(
        loader = { id -> sportsDb.lastEvents(id).mapNotNull { it.toMatch() } },
        dispatcher = dispatcher,
        clock = clock,
        staleAfter = 5.minutes,
        expireAfter = 1.hours,
        backgroundScope = backgroundScope,
    )

    override fun nextMatch(teamId: String): Flow<Loadable<Match?>> =
        nextMatchCache.cachedResource(teamId).map { loadable ->
            loadable.map { matches -> matches.minByOrNull { it.kickoff } }
        }.flowOn(dispatcher)

    override fun lastResult(teamId: String): Flow<Loadable<Match?>> =
        lastResultCache.cachedResource(teamId).map { loadable ->
            loadable.map { matches -> matches.maxByOrNull { it.kickoff } }
        }.flowOn(dispatcher)

    /** The season now being played, falling back to the last one while the new one is unpublished. */
    override fun season(): Flow<Loadable<Season>> = flow {
        val seasonId = resolveSeasonId()
        backgroundScope.launch { runCatching { cacheFinishedSeason(previousSeasonId(seasonId)) } }

        // A season is 380 fixtures: grouping and sorting them belongs off the collector's context,
        // which is the main dispatcher.
        val stored = seasonDao.observeSeason(seasonId)
            .map { rows -> rows.takeIf { it.isNotEmpty() }?.toSeason(seasonId) }
            .flowOn(dispatcher)

        emitAll(
            persistedResource(stored = stored) {
                syncer.sync(key = seasonKey(seasonId), ttl = ttlFor(seasonId)) { fetch(seasonId) }
            },
        )
    }

    override suspend fun refresh() = withContext(dispatcher) {
        val seasonId = resolveSeasonId()
        syncer.sync(key = seasonKey(seasonId), ttl = null, force = true) { fetch(seasonId) }
        nextMatchCache.invalidateAll()
        lastResultCache.invalidateAll()
    }

    override fun match(id: String): Flow<Loadable<SeasonMatch?>> = seasonDao.observeMatch(id)
        .map<SeasonMatchEntity?, Loadable<SeasonMatch?>> { entity ->
            Loadable.Content(entity?.toSeasonMatch())
        }
        .onStart { emit(Loadable.Loading) }
        .flowOn(dispatcher)

    /**
     * A season the calendar does not show, kept because it never changes again.
     *
     * One request in the lifetime of an install, and it is on disk for good.
     */
    private suspend fun cacheFinishedSeason(seasonId: String) {
        syncer.sync(key = seasonKey(seasonId), ttl = null) { fetch(seasonId) }
    }

    private suspend fun resolveSeasonId(): String {
        val current = seasonIdAt(clock.now().toEpochMilliseconds())
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
        if (seasonId == seasonIdAt(clock.now().toEpochMilliseconds())) CurrentSeasonTtl else null

    private fun seasonKey(seasonId: String) = "season:$seasonId"

    private companion object {
        val CurrentSeasonTtl = 6.hours
    }
}
