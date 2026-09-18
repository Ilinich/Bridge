package com.begoml.bridge.feature.squad.di

import com.begoml.bridge.feature.squad.SquadComponent
import com.begoml.bridge.feature.squad.SquadNavigationEntry
import com.begoml.bridge.navigation.FeatureNavigationEntry
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * What this feature contributes to a Compose host.
 *
 * Separate from [squadModule] because both things here are screens: a navigation entry exists where
 * Compose does, and a ViewModel needs a store only the Android host provides. The iOS graph has
 * use for neither — it holds components.
 */
fun squadUiModule() = module {
    single { SquadNavigationEntry() } bind FeatureNavigationEntry::class

    viewModel { get<SquadComponent>().viewModel }
}
