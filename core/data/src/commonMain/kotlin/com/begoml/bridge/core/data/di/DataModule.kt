package com.begoml.bridge.core.data.di

import com.begoml.bridge.core.data.network.createHttpClient
import com.begoml.bridge.core.data.openfootball.SeasonApi
import com.begoml.bridge.core.data.repository.ClubRepository
import com.begoml.bridge.core.data.repository.MatchRepository
import com.begoml.bridge.core.data.repository.SquadRepository
import com.begoml.bridge.core.data.sportsdb.SportsDbApi
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.time.Clock

/** The club this build follows. Swapping it is the only change a fork of another club needs. */
private const val TeamId = "133610"
private const val ClubName = "Chelsea"

val IoDispatcher = named("io")
val DataScope = named("data")

fun dataModule(ioDispatcher: CoroutineDispatcher) = module {
    single<CoroutineDispatcher>(IoDispatcher) { ioDispatcher }
    single<CoroutineScope>(DataScope) { CoroutineScope(SupervisorJob() + get<CoroutineDispatcher>(IoDispatcher)) }
    single<HttpClient> { createHttpClient() }
    single { SportsDbApi(get()) }
    single { SeasonApi(get()) }

    single {
        ClubRepository(
            teamId = TeamId,
            api = get(),
            dispatcher = get(IoDispatcher),
            backgroundScope = get(DataScope),
            nowMillis = { Clock.System.now().toEpochMilliseconds() },
        )
    }
    single {
        MatchRepository(
            teamId = TeamId,
            clubName = ClubName,
            sportsDb = get(),
            seasonApi = get(),
            dispatcher = get(IoDispatcher),
            backgroundScope = get(DataScope),
            nowMillis = { Clock.System.now().toEpochMilliseconds() },
        )
    }
    single {
        SquadRepository(
            teamId = TeamId,
            api = get(),
            dispatcher = get(IoDispatcher),
            backgroundScope = get(DataScope),
            nowMillis = { Clock.System.now().toEpochMilliseconds() },
        )
    }
}
