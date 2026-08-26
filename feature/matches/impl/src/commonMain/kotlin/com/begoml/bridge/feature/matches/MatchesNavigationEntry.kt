package com.begoml.bridge.feature.matches

import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.begoml.bridge.core.data.repository.MatchRepository
import com.begoml.bridge.feature.club.api.ClubRoute
import com.begoml.bridge.feature.matches.api.MatchDetailRoute
import com.begoml.bridge.feature.matches.api.MatchdayRoute
import com.begoml.bridge.feature.matches.api.SeasonRoute
import com.begoml.bridge.feature.matches.detail.MatchDetailScreen
import com.begoml.bridge.feature.matches.matchday.MatchdayFeature
import com.begoml.bridge.feature.matches.matchday.MatchdayScreen
import com.begoml.bridge.feature.matches.season.SeasonFeature
import com.begoml.bridge.feature.matches.season.SeasonScreen
import com.begoml.bridge.navigation.FeatureNavigationEntry
import com.begoml.bridge.navigation.router.AppRouter
import com.begoml.bridge.navigation.router.navigateTo
import com.begoml.bridge.navigation.router.navigateUp
import com.begoml.bridge.navigation.swipeBackMetadata
import kotlinx.coroutines.CoroutineScope
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

internal class MatchesNavigationEntry(private val router: AppRouter) : FeatureNavigationEntry {

    override fun register(scope: EntryProviderScope<NavKey>) {
        scope.entry<MatchdayRoute> {
            val coroutineScope: CoroutineScope = rememberCoroutineScope()
            val feature: MatchdayFeature = koinInject { parametersOf(coroutineScope) }
            MatchdayScreen(
                feature = feature,
                // Reaches another feature by naming its destination. The club screen, its state
                // holder and its module all stay invisible from here.
                onStadiumClick = { router.navigateTo(ClubRoute) },
            )
        }

        scope.entry<SeasonRoute> {
            val coroutineScope: CoroutineScope = rememberCoroutineScope()
            val feature: SeasonFeature = koinInject { parametersOf(coroutineScope) }
            SeasonScreen(
                feature = feature,
                onMatchClick = { match -> router.navigateTo(MatchDetailRoute(match.id)) },
            )
        }

        scope.entry<MatchDetailRoute>(metadata = swipeBackMetadata()) { route ->
            val repository: MatchRepository = koinInject()
            MatchDetailScreen(
                matchId = route.matchId,
                repository = repository,
                onBack = router::navigateUp,
            )
        }
    }
}
