package com.begoml.bridge.core.features.following.impl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okio.Path.Companion.toPath
import org.koin.core.module.Module

internal const val FollowingFileName = "following.preferences_pb"

private val FollowedPlayers = stringSetPreferencesKey("followed_players")

/** A named type rather than a bare String, so the graph cannot hand this to something else. */
internal data class FollowingPath(val value: String)

/** Where the platform lets an app keep a file of its own. */
internal expect fun Module.bindFollowingPath()

internal class DataStoreFollowing(
    private val store: DataStore<Preferences>,
) : FollowingStore {

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

internal actual fun Module.bindFollowingStore() {
    bindFollowingPath()
    single<DataStore<Preferences>> {
        PreferenceDataStoreFactory.createWithPath { get<FollowingPath>().value.toPath() }
    }
    single<FollowingStore> { DataStoreFollowing(get()) }
}
