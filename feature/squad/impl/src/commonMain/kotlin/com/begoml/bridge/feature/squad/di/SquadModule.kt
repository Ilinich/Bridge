package com.begoml.bridge.feature.squad.di

import com.begoml.bridge.core.data.di.IoDispatcher
import com.begoml.bridge.feature.squad.PlayerViewModel
import com.begoml.bridge.feature.squad.grid.SquadDelegate
import com.begoml.bridge.foundation.tessera.stateHolderScope
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
            delegate = SquadDelegate(scope = scope, repository = get()),
            router = get(),
            analytics = get(),
        )
    }
    viewModel {
        val scope = stateHolderScope()
        PlayerViewModel(
            scope = scope,
            delegate = SquadDelegate(scope = scope, repository = get()),
            router = get(),
            ioDispatcher = get(IoDispatcher),
        )
    }
    single { SquadNavigationEntry() } bind FeatureNavigationEntry::class
    single { SquadRouteCodec() } bind RouteCodec::class
}
