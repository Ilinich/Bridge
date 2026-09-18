package com.begoml.bridge.feature.matches.di

import com.begoml.bridge.foundation.coroutines.DispatcherProvider
import com.begoml.bridge.feature.matches.MatchesNavigationEntry
import com.begoml.bridge.feature.matches.api.MatchesRouteCodec
import com.begoml.bridge.feature.matches.detail.MatchDetailComponent
import com.begoml.bridge.feature.matches.detail.MatchDetailViewModel
import com.begoml.bridge.feature.matches.matchday.MatchdayComponent
import com.begoml.bridge.feature.matches.matchday.MatchdayFeature
import com.begoml.bridge.feature.matches.matchday.MatchdayViewModel
import com.begoml.bridge.feature.matches.season.SeasonComponent
import com.begoml.bridge.feature.matches.season.SeasonFeature
import com.begoml.bridge.feature.matches.season.SeasonViewModel
import com.begoml.bridge.foundation.coroutines.stateHolderScope
import com.begoml.bridge.navigation.FeatureNavigationEntry
import com.begoml.bridge.navigation.RouteCodec
import org.koin.core.parameter.parametersOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.core.module.Module
import org.koin.dsl.module

fun matchesModule() = module {
    matchday()
    season()
    matchDetail()

    single { MatchesNavigationEntry() } bind FeatureNavigationEntry::class
    single { MatchesRouteCodec() } bind RouteCodec::class
}

private fun Module.matchday() {
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
}

private fun Module.season() {
        factory {
            val scope = stateHolderScope()
            SeasonComponent(
                scope = scope,
                viewModel = SeasonViewModel(
                    scope = scope,
                    feature = SeasonFeature(scope = scope, matchRepository = get(), clock = get()),
                    connectivity = get(),
                    club = get(),
                    router = get(),
                    ioDispatcher = get<DispatcherProvider>().io,
                    logger = get(),
                    analytics = get(),
                ),
            )
        }
        viewModel { get<SeasonComponent>().viewModel }
}

private fun Module.matchDetail() {
        factory { (matchId: String) ->
            val scope = stateHolderScope()
            MatchDetailComponent(
                scope = scope,
                viewModel = MatchDetailViewModel(
                    matchId = matchId,
                    scope = scope,
                    matchRepository = get(),
                    club = get(),
                    router = get(),
                    ioDispatcher = get<DispatcherProvider>().io,
                    logger = get(),
                ),
            )
        }
        viewModel { (matchId: String) ->
            get<MatchDetailComponent> { parametersOf(matchId) }.viewModel
        }
}
