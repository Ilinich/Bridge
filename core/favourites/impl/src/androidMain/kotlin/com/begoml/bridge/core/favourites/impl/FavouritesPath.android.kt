package com.begoml.bridge.core.favourites.impl

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module

internal actual fun Module.bindFavouritesPath() {
    single { FavouritesPath(androidContext().filesDir.resolve(FavouritesFileName).absolutePath) }
}
