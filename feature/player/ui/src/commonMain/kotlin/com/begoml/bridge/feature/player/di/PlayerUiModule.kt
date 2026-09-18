package com.begoml.bridge.feature.player.di

import com.begoml.bridge.feature.player.PlayerComponent
import com.begoml.bridge.feature.player.PlayerNavigationEntry
import com.begoml.bridge.navigation.FeatureNavigationEntry
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * What this feature contributes to a Compose host.
 *
 * Separate from [playerModule] because a navigation entry is a screen: it exists where Compose
 * does, and the iOS graph has no use for it.
 */
fun playerUiModule() = module {
    single { PlayerNavigationEntry() } bind FeatureNavigationEntry::class

    // One wiring, two ways in: Compose asks for the state holder and lets its store clear it,
    // a Swift screen asks the component and closes it itself.
    viewModel { get<PlayerComponent>().viewModel }
}
