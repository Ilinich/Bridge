package com.begoml.bridge.feature.matches.di

import com.begoml.bridge.core.data.di.IoDispatcher
import com.begoml.bridge.feature.matches.MatchesNavigationEntry
import com.begoml.bridge.feature.matches.api.MatchesRouteCodec
import com.begoml.bridge.feature.matches.detail.MatchDetailViewModel
import com.begoml.bridge.feature.matches.matchday.MatchdayViewModel
import com.begoml.bridge.feature.matches.season.SeasonViewModel
import com.begoml.bridge.navigation.FeatureNavigationEntry
import com.begoml.bridge.navigation.RouteCodec
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module
import kotlin.time.Clock

private fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()

fun matchesModule() = module {
    viewModel {
        MatchdayViewModel(
            clubRepository = get(),
            matchRepository = get(),
            nowMillis = ::nowMillis,
            router = get(),
            ioDispatcher = get(IoDispatcher),
        )
    }
    viewModel {
        SeasonViewModel(
            matchRepository = get(),
            nowMillis = ::nowMillis,
            router = get(),
            ioDispatcher = get(IoDispatcher),
        )
    }
    viewModel { (matchId: String) ->
        MatchDetailViewModel(
            matchId = matchId,
            matchRepository = get(),
            router = get(),
            ioDispatcher = get(IoDispatcher),
        )
    }
    single { MatchesNavigationEntry() } bind FeatureNavigationEntry::class
    single { MatchesRouteCodec() } bind RouteCodec::class
}
