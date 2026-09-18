package com.begoml.bridge.feature.club.di

import com.begoml.bridge.feature.club.ClubComponent
import com.begoml.bridge.feature.club.ClubNavigationEntry
import com.begoml.bridge.navigation.FeatureNavigationEntry
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * What this feature contributes to a Compose host.
 *
 * Separate from [clubModule] because both things here are screens: a navigation entry exists where
 * Compose does, and a ViewModel needs a store only the Android host provides. The iOS graph has
 * use for neither — it holds components.
 */
fun clubUiModule() = module {
    single { ClubNavigationEntry() } bind FeatureNavigationEntry::class

    viewModel { get<ClubComponent>().viewModel }
}
