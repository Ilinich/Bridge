package com.begoml.bridge.feature.squad.di

import com.begoml.bridge.feature.squad.grid.SquadDelegate
import kotlinx.coroutines.CoroutineScope
import org.koin.dsl.module

fun squadModule() = module {
    factory { (scope: CoroutineScope) -> SquadDelegate(scope = scope, repository = get()) }
}
