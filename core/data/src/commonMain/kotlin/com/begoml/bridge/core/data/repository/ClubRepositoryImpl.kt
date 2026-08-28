package com.begoml.bridge.core.data.repository

import com.begoml.bridge.foundation.resource.persistedResource
import com.begoml.bridge.core.domain.repository.ClubRepository

import com.begoml.bridge.core.data.db.ClubDao
import com.begoml.bridge.core.data.db.Syncer
import com.begoml.bridge.core.data.db.VenueDao
import com.begoml.bridge.core.data.db.toClub
import com.begoml.bridge.core.data.db.toEntity
import com.begoml.bridge.core.data.db.toVenue
import com.begoml.bridge.core.domain.model.Club
import com.begoml.bridge.foundation.resource.Loadable
import com.begoml.bridge.core.domain.model.Venue
import com.begoml.bridge.core.data.mapper.toClub
import com.begoml.bridge.core.data.mapper.toVenue
import com.begoml.bridge.core.data.remote.sportsdb.SportsDbApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.days

internal class ClubRepositoryImpl(
    private val api: SportsDbApi,
    private val dao: ClubDao,
    private val venueDao: VenueDao,
    private val syncer: Syncer,
    private val dispatcher: CoroutineDispatcher,
) : ClubRepository {

    // flowOn on the whole chain, not on the stored half of it: everything here — the database
    // flow, the mapping, the fetch that persistedResource starts — belongs off the collector's
    // context, and a repository that inherited it would behave differently per caller.
    override fun club(teamId: String): Flow<Loadable<Club>> = persistedResource(
        stored = dao.observe(teamId).map { entity -> entity?.toClub() },
    ) {
        syncer.sync(key = clubKey(teamId), ttl = ClubTtl) { fetchClub(teamId) }
    }.flowOn(dispatcher)

    @OptIn(ExperimentalCoroutinesApi::class)
    /**
     * The ground, looked up once the club record says which ground to ask about.
     *
     * Keyed on the venue id and not on the club: Room re-emits the club on every upsert, and
     * flatMapLatest would cancel an in-flight venue request and start it again each time — under
     * repeated refreshes it would never finish.
     */
    override fun venue(teamId: String): Flow<Loadable<Venue>> = club(teamId)
        .map { loadable -> (loadable as? Loadable.Content)?.value?.details?.venueId }
        .distinctUntilChanged()
        .flatMapLatest { venueId ->
            if (venueId == null) return@flatMapLatest flowOf(Loadable.Loading)

            persistedResource(
                stored = venueDao.observe(venueId).map { it?.toVenue() },
            ) {
                syncer.sync(key = "venue:$venueId", ttl = ClubTtl) { fetchVenue(venueId) }
            }
        }
        .flowOn(dispatcher)

    override suspend fun refresh(teamId: String) = withContext(dispatcher) {
        syncer.sync(key = clubKey(teamId), ttl = null, force = true) { fetchClub(teamId) }
        val venueId = dao.observe(teamId).first()?.venueId ?: return@withContext
        syncer.sync(key = "venue:$venueId", ttl = null, force = true) { fetchVenue(venueId) }
    }

    private suspend fun fetchClub(teamId: String) {
        val club = api.team(teamId)?.toClub() ?: return
        dao.upsert(club.toEntity())
    }

    private suspend fun fetchVenue(venueId: String) {
        val venue = api.venue(venueId)?.toVenue() ?: return
        venueDao.upsert(venue.toEntity())
    }

    private fun clubKey(teamId: String) = "club:$teamId"

    private companion object {
        val ClubTtl = 7.days
    }
}
