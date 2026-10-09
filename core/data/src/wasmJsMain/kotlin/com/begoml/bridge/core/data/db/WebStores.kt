package com.begoml.bridge.core.data.db

import com.begoml.bridge.core.domain.model.Club
import com.begoml.bridge.core.domain.model.Player
import com.begoml.bridge.core.domain.model.SeasonMatch
import com.begoml.bridge.core.domain.model.Venue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

/**
 * The stores as object stores in the browser, mirrored in memory.
 *
 * The mirror is not an optimisation, it is the contract: IndexedDB has no change notification, so
 * a `Flow` over it has to be driven by whoever writes. Every read is served from the mirror and
 * every write updates the mirror first and then persists — which also makes a write atomic from
 * the screen's point of view without a transaction, because the mirror is a single assignment.
 *
 * Each collection is one key. A squad and a season are replaced wholesale by the repositories
 * that own them, so storing them row by row would buy nothing and cost a transaction.
 */
internal class WebStore(private val db: IndexedDb?) {

    private val json = Json { ignoreUnknownKeys = true }

    val club = MutableStateFlow<ClubRecord?>(null)
    val venues = MutableStateFlow<Map<String, VenueRecord>>(emptyMap())
    val players = MutableStateFlow<List<PlayerRecord>>(emptyList())
    val seasons = MutableStateFlow<Map<String, List<SeasonMatchRecord>>>(emptyMap())
    val freshness = MutableStateFlow<Map<String, Long>>(emptyMap())

    /**
     * Fills the mirror from what the browser kept.
     *
     * A failure here is not fatal and is not reported: storage can be unavailable — private
     * browsing, a blocked origin, a quota — and the app's own answer to having nothing stored is
     * to fetch, which is the same answer. [db] is null when opening failed at all.
     */
    suspend fun restore() {
        val source = db ?: return
        runCatching {
            source.read(Stores.Club, ClubKey)?.let { club.value = json.decodeFromString(it) }
            source.read(Stores.Venue, AllKey)?.let { venues.value = json.decodeFromString(it) }
            source.read(Stores.Player, AllKey)?.let { players.value = json.decodeFromString(it) }
            source.read(Stores.Season, AllKey)?.let { seasons.value = json.decodeFromString(it) }
            source.read(Stores.Freshness, AllKey)?.let { freshness.value = json.decodeFromString(it) }
        }
    }

    private suspend inline fun <reified T> persist(store: String, key: String, value: T) {
        val source = db ?: return
        runCatching { source.write(store, key, json.encodeToString(value)) }
    }

    suspend fun putClub(record: ClubRecord) {
        club.value = record
        persist(Stores.Club, ClubKey, record)
    }

    suspend fun putVenue(record: VenueRecord) {
        venues.value = venues.value + (record.id to record)
        persist(Stores.Venue, AllKey, venues.value)
    }

    suspend fun putPlayers(records: List<PlayerRecord>) {
        players.value = records
        persist(Stores.Player, AllKey, records)
    }

    suspend fun putSeason(season: String, records: List<SeasonMatchRecord>) {
        seasons.value = seasons.value + (season to records)
        persist(Stores.Season, AllKey, seasons.value)
    }

    suspend fun stamp(key: String, millis: Long) {
        freshness.value = freshness.value + (key to millis)
        persist(Stores.Freshness, AllKey, freshness.value)
    }

    private companion object {
        /** One club per build, so its record needs no identity of its own. */
        const val ClubKey = "followed"
        const val AllKey = "all"
    }
}

internal class WebClubStore(private val store: WebStore) : ClubStore {

    override fun observe(id: String): Flow<Club?> =
        store.club.map { record -> record?.takeIf { it.id == id }?.toClub() }

    override suspend fun put(club: Club) = store.putClub(club.toRecord())
}

internal class WebVenueStore(private val store: WebStore) : VenueStore {

    override fun observe(id: String): Flow<Venue?> = store.venues.map { it[id]?.toVenue() }

    override suspend fun put(venue: Venue) = store.putVenue(venue.toRecord())
}

internal class WebPlayerStore(private val store: WebStore) : PlayerStore {

    override fun observeAll(): Flow<List<Player>> =
        store.players.map { records -> records.map { it.toPlayer() } }

    override suspend fun replaceAll(players: List<Player>) =
        store.putPlayers(players.map { it.toRecord() })
}

internal class WebSeasonStore(private val store: WebStore) : SeasonStore {

    override fun observeSeason(season: String): Flow<List<SeasonMatch>> = store.seasons
        .map { seasons -> seasons[season].orEmpty().map { it.toSeasonMatch() } }

    /**
     * A match is found by scanning the seasons in the mirror.
     *
     * A second index keyed by match id would be the relational answer; here the whole calendar is
     * a few hundred records already in memory, and an index is another thing to keep true.
     */
    override fun observeMatch(id: String): Flow<SeasonMatch?> = store.seasons
        .map { seasons ->
            seasons.values.firstNotNullOfOrNull { matches ->
                matches.firstOrNull { it.id == id }
            }?.toSeasonMatch()
        }

    override suspend fun count(season: String): Int = store.seasons.first()[season].orEmpty().size

    override suspend fun replaceSeason(season: String, matches: List<SeasonMatch>) =
        store.putSeason(season, matches.map { it.toRecord() })
}

internal class WebFreshnessStore(private val store: WebStore) : FreshnessStore {

    override suspend fun fetchedAt(key: String): Long? = store.freshness.first()[key]

    override suspend fun stamp(key: String, fetchedAtMillis: Long) = store.stamp(key, fetchedAtMillis)
}
