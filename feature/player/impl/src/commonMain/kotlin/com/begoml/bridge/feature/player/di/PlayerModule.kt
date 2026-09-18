package com.begoml.bridge.feature.player.di

import com.begoml.bridge.foundation.coroutines.DispatcherProvider
import com.begoml.bridge.feature.player.PlayerComponent
import com.begoml.bridge.feature.player.PlayerViewModel
import com.begoml.bridge.feature.player.api.PlayerRouteCodec
import com.begoml.bridge.foundation.coroutines.stateHolderScope
import com.begoml.bridge.navigation.RouteCodec
import org.koin.dsl.bind
import org.koin.dsl.module

fun playerModule() = module {
    factory {
        val scope = stateHolderScope()
        PlayerComponent(
            scope = scope,
            viewModel = PlayerViewModel(
            scope = scope,
            repository = get(),
            club = get(),
            following = get(),
            router = get(),
            ioDispatcher = get<DispatcherProvider>().io,
                logger = get(),
            ),
        )
    }
    single { PlayerRouteCodec() } bind RouteCodec::class
}
