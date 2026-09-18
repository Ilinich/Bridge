package com.begoml.bridge.core.data.di

import com.begoml.bridge.foundation.coroutines.DispatcherProvider
import com.begoml.bridge.core.data.db.BridgeDatabase
import com.begoml.bridge.core.data.db.ClubDao
import com.begoml.bridge.core.data.db.DatabaseFactory
import com.begoml.bridge.core.data.db.FreshnessDao
import com.begoml.bridge.core.data.db.PlayerDao
import com.begoml.bridge.core.data.db.SeasonDao
import com.begoml.bridge.core.data.db.VenueDao
import com.begoml.bridge.core.data.db.Syncer
import com.begoml.bridge.core.data.db.platformDatabaseModule
import com.begoml.bridge.core.data.remote.createHttpClient
import com.begoml.bridge.core.data.remote.openfootball.SeasonApi
import com.begoml.bridge.core.domain.repository.ClubRepository
import com.begoml.bridge.core.data.repository.ClubRepositoryImpl
import com.begoml.bridge.core.domain.repository.MatchRepository
import com.begoml.bridge.core.data.repository.MatchRepositoryImpl
import com.begoml.bridge.core.domain.repository.SquadRepository
import com.begoml.bridge.core.data.repository.SquadRepositoryImpl
import com.begoml.bridge.core.data.remote.sportsdb.SportsDbApi
import io.ktor.client.HttpClient
import com.begoml.bridge.core.domain.model.FollowedClub
import com.begoml.bridge.foundation.coroutines.AppScope
import com.begoml.bridge.foundation.coroutines.PlatformDispatcherProvider
import org.koin.core.module.Module
import org.koin.dsl.module
import kotlin.time.Clock

/** The club this build follows. Swapping this pair is the only change a fork needs. */
private const val TeamId = "133610"
private const val ClubName = "Chelsea"

fun dataModules(): List<Module> = listOf(platformDatabaseModule(), dataModule())

private fun dataModule() = module {
    single<Clock> { Clock.System }
    single { FollowedClub(id = TeamId, name = ClubName) }
    single<DispatcherProvider> { PlatformDispatcherProvider() }
    single { AppScope(get<DispatcherProvider>().io) }

    single<HttpClient> { createHttpClient() }
    single { SportsDbApi(get()) }
    single { SeasonApi(get()) }

    single<BridgeDatabase> { get<DatabaseFactory>().create() }
    single<ClubDao> { get<BridgeDatabase>().clubDao() }
    single<VenueDao> { get<BridgeDatabase>().venueDao() }
    single<PlayerDao> { get<BridgeDatabase>().playerDao() }
    single<SeasonDao> { get<BridgeDatabase>().seasonDao() }
    single<FreshnessDao> { get<BridgeDatabase>().freshnessDao() }
    single {
        Syncer(
            freshness = get(),
            clock = get(),
            dispatcher = get<DispatcherProvider>().io,
        )
    }

    single<ClubRepository> {
        ClubRepositoryImpl(
            api = get(),
            dao = get(),
            venueDao = get(),
            syncer = get(),
            dispatcher = get<DispatcherProvider>().io,
        )
    }
    single<SquadRepository> { SquadRepositoryImpl(
            api = get(),
            dao = get(),
            syncer = get(),
            dispatcher = get<DispatcherProvider>().io,
        ) }
    single<MatchRepository> {
        MatchRepositoryImpl(
            sportsDb = get(),
            seasonApi = get(),
            seasonDao = get(),
            syncer = get(),
            dispatcher = get<DispatcherProvider>().io,
            backgroundScope = get<AppScope>(),
            clock = get(),
        )
    }
}
