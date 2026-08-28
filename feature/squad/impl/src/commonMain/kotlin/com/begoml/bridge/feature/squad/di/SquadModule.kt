package com.begoml.bridge.feature.squad.di

import com.begoml.bridge.foundation.coroutines.DispatcherProvider
import com.begoml.bridge.foundation.coroutines.stateHolderScope
import com.begoml.bridge.feature.squad.SquadNavigationEntry
import com.begoml.bridge.feature.squad.SquadViewModel
import com.begoml.bridge.feature.squad.api.SquadRouteCodec
import com.begoml.bridge.navigation.FeatureNavigationEntry
import com.begoml.bridge.navigation.RouteCodec
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

fun squadModule() = module {
    viewModel {
        val scope = stateHolderScope()
        SquadViewModel(
            scope = scope,
            repository = get(),
            club = get(),
            following = get(),
            router = get(),
            ioDispatcher = get<DispatcherProvider>().io,
            analytics = get(),
        )
    }
    single { SquadNavigationEntry() } bind FeatureNavigationEntry::class
    single { SquadRouteCodec() } bind RouteCodec::class
}
