package com.begoml.bridge.feature.matches.di

import com.begoml.bridge.core.data.di.IoDispatcher
import com.begoml.bridge.feature.matches.MatchesNavigationEntry
import com.begoml.bridge.feature.matches.api.MatchesRouteCodec
import com.begoml.bridge.feature.matches.detail.MatchDetailViewModel
import com.begoml.bridge.feature.matches.matchday.MatchdayFeature
import com.begoml.bridge.feature.matches.matchday.MatchdayViewModel
import com.begoml.bridge.feature.matches.season.SeasonFeature
import com.begoml.bridge.feature.matches.season.SeasonViewModel
import com.begoml.bridge.foundation.tessera.stateHolderScope
import com.begoml.bridge.navigation.FeatureNavigationEntry
import com.begoml.bridge.navigation.RouteCodec
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

fun matchesModule() = module {
    viewModel {
        val scope = stateHolderScope()
        MatchdayViewModel(
            scope = scope,
            feature = MatchdayFeature(
                scope = scope,
                clubRepository = get(),
                matchRepository = get(),
            ),
            connectivity = get(),
            clock = get(),
            router = get(),
            ioDispatcher = get(IoDispatcher),
        )
    }
    viewModel {
        val scope = stateHolderScope()
        SeasonViewModel(
            scope = scope,
            feature = SeasonFeature(scope = scope, matchRepository = get(), clock = get()),
            connectivity = get(),
            matchRepository = get(),
            router = get(),
            ioDispatcher = get(IoDispatcher),
            analytics = get(),
        )
    }
    viewModel { (matchId: String) ->
        MatchDetailViewModel(
            matchId = matchId,
            scope = stateHolderScope(),
            matchRepository = get(),
            router = get(),
            ioDispatcher = get(IoDispatcher),
        )
    }
    single { MatchesNavigationEntry() } bind FeatureNavigationEntry::class
    single { MatchesRouteCodec() } bind RouteCodec::class
}
