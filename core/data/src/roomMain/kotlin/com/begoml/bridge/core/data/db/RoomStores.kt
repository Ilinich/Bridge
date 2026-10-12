package com.begoml.bridge.core.data.db

import com.begoml.bridge.core.domain.model.Club
import com.begoml.bridge.core.domain.model.Player
import com.begoml.bridge.core.domain.model.SeasonMatch
import com.begoml.bridge.core.domain.model.Venue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * The stores as rows in SQLite.
 *
 * Each one is the DAO plus the mapping that keeps entities out of the repositories; the DAOs
 * themselves stay narrow enough that Room generates everything they do.
 */
internal class RoomClubStore(private val dao: ClubDao) : ClubStore {

    override fun observe(id: String): Flow<Club?> = dao.observe(id).map { it?.toClub() }

    override suspend fun put(club: Club) = dao.upsert(club.toEntity())
}

internal class RoomVenueStore(private val dao: VenueDao) : VenueStore {

    override fun observe(id: String): Flow<Venue?> = dao.observe(id).map { it?.toVenue() }

    override suspend fun put(venue: Venue) = dao.upsert(venue.toEntity())
}

internal class RoomPlayerStore(private val dao: PlayerDao) : PlayerStore {

    override fun observeAll(): Flow<List<Player>> =
        dao.observeAll().map { rows -> rows.map { it.toPlayer() } }

    override suspend fun replaceAll(players: List<Player>) =
        dao.replaceAll(players.mapIndexed { index, player -> player.toEntity(index) })
}

internal class RoomSeasonStore(private val dao: SeasonDao) : SeasonStore {

    override fun observeSeason(season: String): Flow<List<SeasonMatch>> =
        dao.observeSeason(season).map { rows -> rows.map { it.toSeasonMatch() } }

    override fun observeMatch(id: String): Flow<SeasonMatch?> =
        dao.observeMatch(id).map { it?.toSeasonMatch() }

    override suspend fun count(season: String): Int = dao.count(season)

    override suspend fun replaceSeason(season: String, matches: List<SeasonMatch>) =
        dao.replaceSeason(season, matches.map { it.toEntity(season) })
}

internal class RoomFreshnessStore(private val dao: FreshnessDao) : FreshnessStore {

    override suspend fun fetchedAt(key: String): Long? = dao.fetchedAt(key)

    override suspend fun stamp(key: String, fetchedAtMillis: Long) =
        dao.stamp(FreshnessEntity(key = key, fetchedAtMillis = fetchedAtMillis))
}
