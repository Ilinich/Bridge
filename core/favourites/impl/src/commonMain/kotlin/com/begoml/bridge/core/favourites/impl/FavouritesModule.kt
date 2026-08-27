package com.begoml.bridge.core.favourites.impl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.begoml.bridge.core.favourites.FavouritesFeature
import com.begoml.bridge.foundation.tessera.stateHolderScope
import okio.Path.Companion.toPath
import org.koin.core.module.Module
import org.koin.dsl.module

internal const val FavouritesFileName = "favourites.preferences_pb"

/** A named type rather than a bare String, so the graph cannot hand this to something else. */
internal data class FavouritesPath(val value: String)

/** Where the platform lets an app keep a file of its own. */
internal expect fun Module.bindFavouritesPath()

fun favouritesModule(): Module = module {
    bindFavouritesPath()
    single<DataStore<Preferences>> {
        PreferenceDataStoreFactory.createWithPath { get<FavouritesPath>().value.toPath() }
    }
    single<FavouritesStore> { DataStoreFavourites(get()) }
    single<FavouritesFeature> {
        FavouritesFeatureImpl(scope = stateHolderScope(), store = get())
    }
}
