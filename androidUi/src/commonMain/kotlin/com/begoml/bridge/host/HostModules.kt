package com.begoml.bridge.host

import com.begoml.bridge.feature.club.di.clubUiModule
import com.begoml.bridge.feature.matches.di.matchesUiModule
import com.begoml.bridge.feature.player.di.playerUiModule
import com.begoml.bridge.feature.squad.di.squadUiModule
import org.koin.core.module.Module

/**
 * What the Compose host brings to the graph: a navigation entry and a ViewModel-store definition
 * per feature. The iOS app brings none of it — its screens hold components instead.
 */
fun hostModules(): List<Module> = listOf(
    matchesUiModule(),
    squadUiModule(),
    playerUiModule(),
    clubUiModule(),
)
