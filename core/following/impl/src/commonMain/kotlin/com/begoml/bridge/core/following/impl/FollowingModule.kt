package com.begoml.bridge.core.following.impl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.begoml.bridge.core.following.FollowingFeature
import com.begoml.bridge.foundation.tessera.stateHolderScope
import okio.Path.Companion.toPath
import org.koin.core.module.Module
import org.koin.dsl.module

internal const val FollowingFileName = "following.preferences_pb"

/** A named type rather than a bare String, so the graph cannot hand this to something else. */
internal data class FollowingPath(val value: String)

/** Where the platform lets an app keep a file of its own. */
internal expect fun Module.bindFollowingPath()

fun followingModule(): Module = module {
    bindFollowingPath()
    single<DataStore<Preferences>> {
        PreferenceDataStoreFactory.createWithPath { get<FollowingPath>().value.toPath() }
    }
    single<FollowingStore> { DataStoreFollowing(get()) }
    single<FollowingFeature> {
        FollowingFeatureImpl(scope = stateHolderScope(), store = get())
    }
}
