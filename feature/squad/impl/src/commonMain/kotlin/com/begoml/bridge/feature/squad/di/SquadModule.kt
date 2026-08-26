package com.begoml.bridge.feature.squad.di

import com.begoml.bridge.feature.squad.PlayerViewModel
import com.begoml.bridge.feature.squad.SquadNavigationEntry
import com.begoml.bridge.feature.squad.SquadViewModel
import com.begoml.bridge.feature.squad.api.SquadRouteCodec
import com.begoml.bridge.navigation.FeatureNavigationEntry
import com.begoml.bridge.navigation.RouteCodec
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

fun squadModule() = module {
    viewModel { SquadViewModel(repository = get(), router = get()) }
    viewModel { PlayerViewModel(repository = get(), router = get()) }
    single { SquadNavigationEntry() } bind FeatureNavigationEntry::class
    single { SquadRouteCodec() } bind RouteCodec::class
}
