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
import kotlinx.coroutines.flow.Flow
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
class ClubRepository internal constructor(
    private val teamId: String,
    private val api: SportsDbApi,
    private val dao: ClubDao,
    private val venueDao: VenueDao,
    private val syncer: Syncer,
) {

    fun club(): Flow<Loadable<Club>> = persistedResource(
        stored = dao.observe(teamId).map { entity -> entity?.toClub() },
    ) {
        syncer.sync(key = clubKey, ttl = ClubTtl) { fetchClub() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun venue(): Flow<Loadable<Venue>> = club().flatMapLatest { loadable ->
        val venueId = (loadable as? Loadable.Content)?.value?.details?.venueId
            ?: return@flatMapLatest flowOf(Loadable.Loading)

        persistedResource(stored = venueDao.observe(venueId).map { it?.toVenue() }) {
            syncer.sync(key = "venue:$venueId", ttl = ClubTtl) { fetchVenue(venueId) }
        }
    }

    suspend fun refresh() {
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
