package com.begoml.bridge.core.data.di

import com.begoml.bridge.core.data.db.BridgeDatabase
import com.begoml.bridge.core.data.db.ClubDao
import com.begoml.bridge.core.data.db.DatabaseFactory
import com.begoml.bridge.core.data.db.FreshnessDao
import com.begoml.bridge.core.data.db.PlayerDao
import com.begoml.bridge.core.data.db.SeasonDao
import com.begoml.bridge.core.data.db.VenueDao
import com.begoml.bridge.core.data.db.Syncer
import com.begoml.bridge.core.data.db.platformDatabaseModule
import com.begoml.bridge.core.data.network.createHttpClient
import com.begoml.bridge.core.data.openfootball.SeasonApi
import com.begoml.bridge.core.data.repository.ClubRepository
import com.begoml.bridge.core.data.repository.ClubRepositoryImpl
import com.begoml.bridge.core.data.repository.MatchRepository
import com.begoml.bridge.core.data.repository.MatchRepositoryImpl
import com.begoml.bridge.core.data.repository.SquadRepository
import com.begoml.bridge.core.data.repository.SquadRepositoryImpl
import com.begoml.bridge.core.data.sportsdb.SportsDbApi
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.time.Clock

/** The club this build follows. Swapping it is the only change a fork of another club needs. */
private const val TeamId = "133610"
private const val ClubName = "Chelsea"

val IoDispatcher = named("io")
val DataScope = named("data")

private val nowMillis: () -> Long = { Clock.System.now().toEpochMilliseconds() }

fun dataModules(ioDispatcher: CoroutineDispatcher): List<Module> =
    listOf(platformDatabaseModule(), dataModule(ioDispatcher))

private fun dataModule(ioDispatcher: CoroutineDispatcher) = module {
    single<CoroutineDispatcher>(IoDispatcher) { ioDispatcher }
    single<CoroutineScope>(DataScope) {
        CoroutineScope(SupervisorJob() + get<CoroutineDispatcher>(IoDispatcher))
    }

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
            nowMillis = nowMillis,
            dispatcher = get(IoDispatcher),
        )
    }

    single<ClubRepository> {
        ClubRepositoryImpl(
            teamId = TeamId,
            api = get(),
            dao = get(),
            venueDao = get(),
            syncer = get(),
            dispatcher = get(IoDispatcher),
        )
    }
    single<SquadRepository> { SquadRepositoryImpl(
            teamId = TeamId,
            api = get(),
            dao = get(),
            syncer = get(),
            dispatcher = get(IoDispatcher),
        ) }
    single<MatchRepository> {
        MatchRepositoryImpl(
            teamId = TeamId,
            clubName = ClubName,
            sportsDb = get(),
            seasonApi = get(),
            seasonDao = get(),
            syncer = get(),
            dispatcher = get(IoDispatcher),
            backgroundScope = get(DataScope),
            nowMillis = nowMillis,
        )
    }
}
