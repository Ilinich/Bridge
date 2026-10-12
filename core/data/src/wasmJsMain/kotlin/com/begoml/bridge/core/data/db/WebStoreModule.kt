package com.begoml.bridge.core.data.db

import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * One store object, shared by the five façades over it.
 *
 * It arrives open and already filled: [openWebStorage] runs before the graph is built, so no
 * screen has to decide what a read means while the database is still opening.
 */
actual fun platformStoreModule(): Module = module {
    single { restored ?: WebStore(db = null) }
    single<ClubStore> { WebClubStore(get()) }
    single<VenueStore> { WebVenueStore(get()) }
    single<PlayerStore> { WebPlayerStore(get()) }
    single<SeasonStore> { WebSeasonStore(get()) }
    single<FreshnessStore> { WebFreshnessStore(get()) }
}

private var restored: WebStore? = null

/**
 * Opens storage and reads what the last visit left, before the app asks anything of it.
 *
 * Called from the web entry point. A failure is not fatal: storage can be unavailable — private
 * browsing, a blocked origin, a quota — and the app's answer to having nothing stored is to
 * fetch, which is the same answer it already has.
 */
suspend fun openWebStorage(name: String = "bridge") {
    val database = runCatching { IndexedDb.open(name) }.getOrNull()
    restored = WebStore(database).also { it.restore() }
}
