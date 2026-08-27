package com.begoml.bridge.core.favourites.impl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val FollowedPlayers = stringSetPreferencesKey("followed_players")

/**
 * The set on disk.
 *
 * Separate from the cached feeds in the database on purpose: those are re-fetchable and are
 * dropped wholesale on a schema change, while this is the only thing in the app a person authored.
 */
internal interface FavouritesStore {

    fun observe(): Flow<Set<String>>

    suspend fun toggle(playerId: String)
}

internal class DataStoreFavourites(
    private val store: DataStore<Preferences>,
) : FavouritesStore {

    override fun observe(): Flow<Set<String>> =
        store.data.map { preferences -> preferences[FollowedPlayers].orEmpty() }

    override suspend fun toggle(playerId: String) {
        store.edit { preferences ->
            val current = preferences[FollowedPlayers].orEmpty()
            preferences[FollowedPlayers] =
                if (playerId in current) current - playerId else current + playerId
        }
    }
}
