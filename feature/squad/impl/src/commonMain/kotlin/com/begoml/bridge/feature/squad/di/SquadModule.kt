package com.begoml.bridge.feature.squad.di

import com.begoml.bridge.foundation.coroutines.DispatcherProvider
import com.begoml.bridge.foundation.coroutines.stateHolderScope
import com.begoml.bridge.feature.squad.SquadComponent
import com.begoml.bridge.feature.squad.SquadViewModel
import com.begoml.bridge.feature.squad.api.SquadRouteCodec
import com.begoml.bridge.navigation.RouteCodec
import org.koin.dsl.bind
import org.koin.dsl.module

fun squadModule() = module {
    factory {
        val scope = stateHolderScope()
        SquadComponent(
            scope = scope,
            viewModel = SquadViewModel(
            scope = scope,
            repository = get(),
            club = get(),
            following = get(),
            router = get(),
            ioDispatcher = get<DispatcherProvider>().io,
            logger = get(),
                analytics = get(),
            ),
        )
    }
    single { SquadRouteCodec() } bind RouteCodec::class
}
