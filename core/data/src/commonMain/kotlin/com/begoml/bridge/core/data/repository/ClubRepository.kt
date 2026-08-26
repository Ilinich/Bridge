package com.begoml.bridge.core.data.repository

import com.begoml.bridge.core.data.db.ClubDao
import com.begoml.bridge.core.data.db.Syncer
import com.begoml.bridge.core.data.db.VenueDao
import com.begoml.bridge.core.data.db.toClub
import com.begoml.bridge.core.data.db.toEntity
import com.begoml.bridge.core.data.db.toVenue
import com.begoml.bridge.core.data.model.Club
import com.begoml.bridge.core.data.model.Loadable
import com.begoml.bridge.core.data.model.Venue
import com.begoml.bridge.core.data.model.toClub
import com.begoml.bridge.core.data.model.toVenue
import com.begoml.bridge.core.data.sportsdb.SportsDbApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlin.time.Duration.Companion.days

/**
 * The club profile and the ground it plays on.
 *
 * Both change about once a year, so both are held for a week. The venue is a second request that
 * only the club screen needs, and it is only made once the club record has told us which ground
 * to ask about.
 */
interface ClubRepository {

    fun club(): Flow<Loadable<Club>>

    fun venue(): Flow<Loadable<Venue>>

    suspend fun refresh()
}

internal class ClubRepositoryImpl(
    private val teamId: String,
    private val api: SportsDbApi,
    private val dao: ClubDao,
    private val venueDao: VenueDao,
    private val syncer: Syncer,
    private val dispatcher: CoroutineDispatcher,
) : ClubRepository {

    override fun club(): Flow<Loadable<Club>> = persistedResource(
        stored = dao.observe(teamId).map { entity -> entity?.toClub() }.flowOn(dispatcher),
    ) {
        syncer.sync(key = clubKey, ttl = ClubTtl) { fetchClub() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    /**
     * The ground, looked up once the club record says which ground to ask about.
     *
     * Keyed on the venue id and not on the club: Room re-emits the club on every upsert, and
     * flatMapLatest would cancel an in-flight venue request and start it again each time — under
     * repeated refreshes it would never finish.
     */
    override fun venue(): Flow<Loadable<Venue>> = club()
        .map { loadable -> (loadable as? Loadable.Content)?.value?.details?.venueId }
        .distinctUntilChanged()
        .flatMapLatest { venueId ->
            if (venueId == null) return@flatMapLatest flowOf(Loadable.Loading)

            persistedResource(
                stored = venueDao.observe(venueId).map { it?.toVenue() }.flowOn(dispatcher),
            ) {
                syncer.sync(key = "venue:$venueId", ttl = ClubTtl) { fetchVenue(venueId) }
            }
        }

    override suspend fun refresh() {
        syncer.sync(key = clubKey, ttl = null, force = true) { fetchClub() }
        val venueId = dao.observe(teamId).first()?.venueId ?: return
        syncer.sync(key = "venue:$venueId", ttl = null, force = true) { fetchVenue(venueId) }
    }

    private suspend fun fetchClub() {
        val club = api.team(teamId)?.toClub() ?: return
        dao.upsert(club.toEntity())
    }

    private suspend fun fetchVenue(venueId: String) {
        val venue = api.venue(venueId)?.toVenue() ?: return
        venueDao.upsert(venue.toEntity())
    }

    private val clubKey get() = "club:$teamId"

    private companion object {
        val ClubTtl = 7.days
    }
}
