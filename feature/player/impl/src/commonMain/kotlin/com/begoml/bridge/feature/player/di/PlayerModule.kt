package com.begoml.bridge.feature.player.di

import com.begoml.bridge.foundation.coroutines.DispatcherProvider
import com.begoml.bridge.feature.player.PlayerDelegate
import com.begoml.bridge.feature.player.PlayerNavigationEntry
import com.begoml.bridge.feature.player.PlayerViewModel
import com.begoml.bridge.feature.player.api.PlayerRouteCodec
import com.begoml.bridge.foundation.coroutines.stateHolderScope
import com.begoml.bridge.navigation.FeatureNavigationEntry
import com.begoml.bridge.navigation.RouteCodec
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

fun playerModule() = module {
    viewModel {
        val scope = stateHolderScope()
        PlayerViewModel(
            scope = scope,
            delegate = PlayerDelegate(scope = scope, repository = get()),
            following = get(),
            router = get(),
            ioDispatcher = get<DispatcherProvider>().io,
        )
    }
    single { PlayerNavigationEntry() } bind FeatureNavigationEntry::class
    single { PlayerRouteCodec() } bind RouteCodec::class
}
