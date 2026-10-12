package com.begoml.bridge.core.data.db

import org.koin.core.module.Module
import org.koin.dsl.module

/** The bindings every Room target shares, on top of its own way of locating the database file. */
internal fun roomStoreModule(factory: Module.() -> Unit): Module = module {
    factory()
    single<BridgeDatabase> { get<DatabaseFactory>().create() }
    single<ClubStore> { RoomClubStore(get<BridgeDatabase>().clubDao()) }
    single<VenueStore> { RoomVenueStore(get<BridgeDatabase>().venueDao()) }
    single<PlayerStore> { RoomPlayerStore(get<BridgeDatabase>().playerDao()) }
    single<SeasonStore> { RoomSeasonStore(get<BridgeDatabase>().seasonDao()) }
    single<FreshnessStore> { RoomFreshnessStore(get<BridgeDatabase>().freshnessDao()) }
}
