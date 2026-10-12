package com.begoml.bridge.core.data.db

import com.begoml.bridge.core.domain.model.Club
import com.begoml.bridge.core.domain.model.Player
import com.begoml.bridge.core.domain.model.Season
import com.begoml.bridge.core.domain.model.SeasonMatch
import com.begoml.bridge.core.domain.model.SeasonRound
import com.begoml.bridge.core.domain.model.Venue
import kotlinx.coroutines.flow.Flow
import org.koin.core.module.Module

/**
 * What the repositories are allowed to ask of storage.
 *
 * They speak domain models rather than rows: a stored shape is the business of whatever stores it,
 * and the two implementations here keep data in very different ways — a relational database on
 * Android, iOS and the desktop, object stores in the browser. Everything the contract does promise
 * is the part a repository actually depends on: a read that keeps emitting as the data changes,
 * and a write that replaces a collection in one step rather than row by row.
 */
interface ClubStore {

    fun observe(id: String): Flow<Club?>

    suspend fun put(club: Club)
}

interface VenueStore {

    fun observe(id: String): Flow<Venue?>

    suspend fun put(venue: Venue)
}

interface PlayerStore {

    fun observeAll(): Flow<List<Player>>

    /** A squad is replaced wholesale: a transferred player must disappear, not linger. */
    suspend fun replaceAll(players: List<Player>)
}

interface SeasonStore {

    fun observeSeason(season: String): Flow<List<SeasonMatch>>

    fun observeMatch(id: String): Flow<SeasonMatch?>

    suspend fun count(season: String): Int

    suspend fun replaceSeason(season: String, matches: List<SeasonMatch>)
}

/**
 * When each resource was last fetched.
 *
 * Stored rather than kept in memory on purpose: after a process death an in-memory stamp would be
 * gone and every cold start would re-fetch, which is exactly what persisting the data avoided.
 */
interface FreshnessStore {

    suspend fun fetchedAt(key: String): Long?

    suspend fun stamp(key: String, fetchedAtMillis: Long)
}

/** Binds the platform's way of storing all of the above. */
expect fun platformStoreModule(): Module

/** Rounds are a shape the screen wants, not a shape anything stores. */
internal fun List<SeasonMatch>.toSeason(name: String): Season = Season(
    name = name,
    rounds = groupBy { it.round }
        .map { (number, matches) -> SeasonRound(number = number, matches = matches) }
        .sortedBy { it.number },
)
