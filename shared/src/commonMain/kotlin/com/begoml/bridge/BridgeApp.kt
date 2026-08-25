package com.begoml.bridge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import bridge.shared.generated.resources.Res
import bridge.shared.generated.resources.tab_matchday
import bridge.shared.generated.resources.tab_season
import bridge.shared.generated.resources.tab_squad
import com.begoml.bridge.core.data.repository.MatchRepository
import com.begoml.bridge.feature.matches.detail.MatchDetailScreen
import com.begoml.bridge.feature.matches.matchday.MatchdayFeature
import com.begoml.bridge.feature.matches.matchday.MatchdayScreen
import com.begoml.bridge.feature.matches.season.SeasonFeature
import com.begoml.bridge.feature.matches.season.SeasonScreen
import com.begoml.bridge.feature.squad.grid.SquadDelegate
import com.begoml.bridge.feature.squad.grid.SquadEvent
import com.begoml.bridge.feature.squad.grid.SquadScreen
import com.begoml.bridge.feature.squad.player.PlayerScreen
import com.begoml.bridge.navigation.BridgeNavDisplay
import com.begoml.bridge.navigation.Route
import com.begoml.bridge.navigation.rememberTabbedBackStack
import com.begoml.bridge.navigation.swipeBackMetadata
import com.begoml.bridge.uikit.LocalScreenPadding
import com.begoml.bridge.uikit.component.BridgeIcon
import com.begoml.bridge.uikit.component.BridgeTab
import com.begoml.bridge.uikit.component.BridgeTabBar
import com.begoml.bridge.uikit.glass.GlassBackdrop
import com.begoml.bridge.uikit.theme.BridgeColors
import com.begoml.bridge.uikit.theme.BridgeTheme
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import androidx.compose.runtime.LaunchedEffect

/** Tab-bar height plus the gap above and below it; screens scroll behind all of it. */
private val TabBarInset = 70.dp
private const val CrossfadeMillis = 150

@Composable
fun App() {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components { add(KtorNetworkFetcherFactory()) }
            .crossfade(CrossfadeMillis)
            .build()
    }

    BridgeTheme {
        val scope = rememberCoroutineScope()
        val backStack = rememberTabbedBackStack()

        val matchday: MatchdayFeature = koinInject { parametersOf(scope) }
        val season: SeasonFeature = koinInject { parametersOf(scope) }
        val squad: SquadDelegate = koinInject { parametersOf(scope) }
        val matchRepository: MatchRepository = koinInject()

        LaunchedEffect(squad) {
            squad.singleEvents.collectLatest { event ->
                when (event) {
                    is SquadEvent.OpenPlayer -> backStack.push(Route.PlayerDetail(event.playerId))
                }
            }
        }

        val tabs = rememberTabs()
        val contentPadding = rememberScreenPadding()

        CompositionLocalProvider(LocalScreenPadding provides contentPadding) {
        GlassBackdrop(
            modifier = Modifier.fillMaxSize(),
            backdrop = {
                Box(modifier = Modifier.fillMaxSize().background(BridgeColors.Ground)) {
                    BridgeNavDisplay(
                        backStack = backStack.current,
                        onBack = backStack::pop,
                        modifier = Modifier.fillMaxSize(),
                    ) { key ->
                        entryFor(
                            key = key,
                            matchday = matchday,
                            season = season,
                            squad = squad,
                            matchRepository = matchRepository,
                            onOpenMatch = { backStack.push(Route.MatchDetail(it)) },
                            onBack = backStack::pop,
                        )
                    }
                }
            },
        ) {
            BridgeTabBar(
                tabs = tabs,
                selectedIndex = backStack.selectedTab,
                onSelect = backStack::selectTab,
            )
        }
        }
    }
}

@Composable
private fun rememberScreenPadding(): PaddingValues {
    val insets = WindowInsets.safeDrawing.asPaddingValues()
    val layoutDirection = LocalLayoutDirection.current
    return PaddingValues(
        top = insets.calculateTopPadding(),
        bottom = insets.calculateBottomPadding() + TabBarInset,
        start = insets.calculateStartPadding(layoutDirection),
        end = insets.calculateEndPadding(layoutDirection),
    )
}

@Composable
private fun rememberTabs(): List<BridgeTab> = listOf(
    BridgeTab(BridgeIcon.Matchday, stringResource(Res.string.tab_matchday)),
    BridgeTab(BridgeIcon.Season, stringResource(Res.string.tab_season)),
    BridgeTab(BridgeIcon.Squad, stringResource(Res.string.tab_squad)),
)

@Suppress("LongParameterList")
private fun entryFor(
    key: NavKey,
    matchday: MatchdayFeature,
    season: SeasonFeature,
    squad: SquadDelegate,
    matchRepository: MatchRepository,
    onOpenMatch: (String) -> Unit,
    onBack: () -> Unit,
): NavEntry<NavKey> = when (key) {
    is Route.Matchday -> NavEntry(key) {
        MatchdayScreen(feature = matchday)
    }

    is Route.Season -> NavEntry(key) {
        SeasonScreen(feature = season, onMatchClick = { match -> onOpenMatch(match.id) })
    }

    is Route.Squad -> NavEntry(key) {
        SquadScreen(delegate = squad)
    }

    is Route.MatchDetail -> NavEntry(key, metadata = swipeBackMetadata()) {
        MatchDetailScreen(matchId = key.matchId, repository = matchRepository, onBack = onBack)
    }

    // The player pager owns a horizontal drag, so it deliberately does not opt into swipe-back.
    is Route.PlayerDetail -> NavEntry(key) {
        PlayerScreen(delegate = squad, initialPlayerId = key.playerId, onBack = onBack)
    }

    else -> error("Unmapped route: $key")
}
