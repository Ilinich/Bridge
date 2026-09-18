package com.begoml.bridge.feature.matches.di

import com.begoml.bridge.feature.matches.detail.MatchDetailComponent
import com.begoml.bridge.feature.matches.matchday.MatchdayComponent
import com.begoml.bridge.feature.matches.season.SeasonComponent
import com.begoml.bridge.feature.matches.MatchesNavigationEntry
import com.begoml.bridge.navigation.FeatureNavigationEntry
import org.koin.core.parameter.parametersOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * What this feature contributes to a Compose host.
 *
 * Separate from [matchesModule] because both things here are screens: a navigation entry exists where
 * Compose does, and a ViewModel needs a store only the Android host provides. The iOS graph has
 * use for neither — it holds components.
 */
fun matchesUiModule() = module {
    single { MatchesNavigationEntry() } bind FeatureNavigationEntry::class

    viewModel { get<MatchdayComponent>().viewModel }
    viewModel { get<SeasonComponent>().viewModel }
    viewModel { (matchId: String) ->
        get<MatchDetailComponent> { parametersOf(matchId) }.viewModel
    }
}
