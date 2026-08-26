package com.begoml.bridge.feature.matches

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.begoml.bridge.feature.matches.api.MatchDetailRoute
import com.begoml.bridge.feature.matches.api.MatchdayRoute
import com.begoml.bridge.feature.matches.api.SeasonRoute
import com.begoml.bridge.feature.matches.detail.MatchDetailScreen
import com.begoml.bridge.feature.matches.detail.MatchDetailViewModel
import com.begoml.bridge.feature.matches.matchday.MatchdayScreen
import com.begoml.bridge.feature.matches.matchday.MatchdayViewModel
import com.begoml.bridge.feature.matches.season.SeasonScreen
import com.begoml.bridge.feature.matches.season.SeasonViewModel
import com.begoml.bridge.navigation.FeatureNavigationEntry
import com.begoml.bridge.navigation.swipeBackMetadata
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Registers this feature's destinations.
 *
 * Nothing here knows where a tap leads: every screen's ViewModel holds the router, so an entry is
 * only a mapping from a route to the screen that draws it.
 */
internal class MatchesNavigationEntry : FeatureNavigationEntry {

    override fun register(scope: EntryProviderScope<NavKey>) {
        scope.entry<MatchdayRoute> {
            MatchdayScreen(viewModel = koinViewModel<MatchdayViewModel>())
        }

        scope.entry<SeasonRoute> {
            SeasonScreen(viewModel = koinViewModel<SeasonViewModel>())
        }

        scope.entry<MatchDetailRoute>(metadata = swipeBackMetadata()) { route ->
            MatchDetailScreen(
                viewModel = koinViewModel<MatchDetailViewModel> { parametersOf(route.matchId) },
            )
        }
    }
}
