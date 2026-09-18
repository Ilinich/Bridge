package com.begoml.bridge.feature.matches.di

import com.begoml.bridge.foundation.coroutines.DispatcherProvider
import com.begoml.bridge.feature.matches.MatchesNavigationEntry
import com.begoml.bridge.feature.matches.api.MatchesRouteCodec
import com.begoml.bridge.feature.matches.detail.MatchDetailViewModel
import com.begoml.bridge.feature.matches.matchday.MatchdayComponent
import com.begoml.bridge.feature.matches.matchday.MatchdayFeature
import com.begoml.bridge.feature.matches.matchday.MatchdayViewModel
import com.begoml.bridge.feature.matches.season.SeasonFeature
import com.begoml.bridge.feature.matches.season.SeasonViewModel
import com.begoml.bridge.foundation.coroutines.stateHolderScope
import com.begoml.bridge.navigation.FeatureNavigationEntry
import com.begoml.bridge.navigation.RouteCodec
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

fun matchesModule() = module {
    factory {
        val scope = stateHolderScope()
        MatchdayComponent(
            scope = scope,
            viewModel = MatchdayViewModel(
                scope = scope,
                feature = MatchdayFeature(
                    scope = scope,
                    club = get(),
                    clubRepository = get(),
                    matchRepository = get(),
                    squadRepository = get(),
                    following = get(),
                ),
                connectivity = get(),
                clock = get(),
                router = get(),
                ioDispatcher = get<DispatcherProvider>().io,
                logger = get(),
            ),
        )
    }
    // One wiring, two ways in: Compose asks for the state holder and lets its store clear it,
    // a Swift screen asks for the component and closes it itself.
    viewModel { get<MatchdayComponent>().viewModel }
    viewModel {
        val scope = stateHolderScope()
        SeasonViewModel(
            scope = scope,
            feature = SeasonFeature(scope = scope, matchRepository = get(), clock = get()),
            connectivity = get(),
            club = get(),
            router = get(),
            ioDispatcher = get<DispatcherProvider>().io,
            logger = get(),
            analytics = get(),
        )
    }
    viewModel { (matchId: String) ->
        MatchDetailViewModel(
            matchId = matchId,
            scope = stateHolderScope(),
            matchRepository = get(),
            club = get(),
            router = get(),
            ioDispatcher = get<DispatcherProvider>().io,
            logger = get(),
        )
    }
    single { MatchesNavigationEntry() } bind FeatureNavigationEntry::class
    single { MatchesRouteCodec() } bind RouteCodec::class
}
