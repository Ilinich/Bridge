package com.begoml.bridge.feature.squad.di

import com.begoml.bridge.feature.squad.SquadNavigationEntry
import com.begoml.bridge.feature.squad.api.SquadRouteCodec
import com.begoml.bridge.feature.squad.grid.SquadDelegate
import com.begoml.bridge.navigation.FeatureNavigationEntry
import com.begoml.bridge.navigation.RouteCodec
import kotlinx.coroutines.CoroutineScope
import org.koin.dsl.bind
import org.koin.dsl.module

fun squadModule() = module {
    factory { (scope: CoroutineScope) -> SquadDelegate(scope = scope, repository = get()) }
    single { SquadNavigationEntry(router = get()) } bind FeatureNavigationEntry::class
    single { SquadRouteCodec() } bind RouteCodec::class
}
