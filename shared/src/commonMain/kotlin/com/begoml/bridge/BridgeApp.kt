package com.begoml.bridge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import bridge.shared.generated.resources.Res
import bridge.shared.generated.resources.tab_club
import bridge.shared.generated.resources.tab_matchday
import bridge.shared.generated.resources.tab_season
import bridge.shared.generated.resources.tab_squad
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import com.begoml.bridge.feature.club.api.ClubRoute
import com.begoml.bridge.feature.matches.api.MatchdayRoute
import com.begoml.bridge.feature.matches.api.SeasonRoute
import com.begoml.bridge.feature.squad.api.SquadRoute
import com.begoml.bridge.foundation.analytics.Analytics
import com.begoml.bridge.foundation.analytics.AnalyticsEvent
import com.begoml.bridge.navigation.BridgeTabPager
import com.begoml.bridge.navigation.FeatureNavigationEntry
import com.begoml.bridge.navigation.router.NavigationHost
import com.begoml.bridge.navigation.Route
import com.begoml.bridge.navigation.RouteCodec
import com.begoml.bridge.navigation.rememberTabbedBackStack
import com.begoml.bridge.uikit.LocalScreenPadding
import com.begoml.bridge.uikit.component.BridgeIcon
import com.begoml.bridge.uikit.component.BridgeTab
import com.begoml.bridge.uikit.component.BridgeTabBar
import com.begoml.bridge.uikit.glass.GlassBackdrop
import com.begoml.bridge.uikit.theme.BridgeColors
import com.begoml.bridge.uikit.theme.BridgeTheme
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.currentKoinScope
import org.koin.compose.koinInject

/** Tab-bar height plus the gap above and below it; screens scroll behind all of it. */
private val TabBarInset = 70.dp
private const val CrossfadeMillis = 150

private val TabRoutes: List<Route> = listOf(MatchdayRoute, SeasonRoute, SquadRoute, ClubRoute)

/**
 * The host.
 *
 * It knows the tabs and nothing else: destinations are contributed by the features themselves, so
 * adding a screen never means editing this file.
 */
@Composable
fun App() {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components { add(KtorNetworkFetcherFactory()) }
            .crossfade(CrossfadeMillis)
            .build()
    }

    BridgeTheme {
        // One graph for the whole function: the Koin taken from the composition, not the global
        // instance, so a test that wraps App() in its own KoinApplication supplies all of these
        // rather than the router alone.
        val koin = currentKoinScope().getKoin()
        val codecs: List<RouteCodec> = remember(koin) { koin.getAll() }
        val entries: List<FeatureNavigationEntry> = remember(koin) { koin.getAll() }
        val host: NavigationHost = koinInject()
        val analytics: Analytics = koinInject()

        LaunchedEffect(analytics) { analytics.track(AnalyticsEvent.AppOpened) }

        val backStack = rememberTabbedBackStack(roots = TabRoutes, codecs = codecs)
        DisposableEffect(backStack) {
            host.attach(backStack)
            onDispose { host.detach() }
        }

        val tabs = bridgeTabs()
        val contentPadding = screenPadding()

        CompositionLocalProvider(LocalScreenPadding provides contentPadding) {
            GlassBackdrop(
                modifier = Modifier.fillMaxSize(),
                backdrop = {
                    Box(modifier = Modifier.fillMaxSize().background(BridgeColors.Ground)) {
                        BridgeTabPager(
                            backStack = backStack,
                            entries = entries,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                },
            ) {
                BridgeTabBar(
                    tabs = tabs,
                    selectedIndex = backStack.selectedTab,
                    onSelect = { index ->
                        analytics.track(AnalyticsEvent.TabSelected(tabs[index].label))
                        backStack.selectTab(index)
                    },
                )
            }
        }
    }
}

@Composable
private fun screenPadding(): PaddingValues {
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
private fun bridgeTabs(): List<BridgeTab> = listOf(
    BridgeTab(BridgeIcon.Matchday, stringResource(Res.string.tab_matchday)),
    BridgeTab(BridgeIcon.Season, stringResource(Res.string.tab_season)),
    BridgeTab(BridgeIcon.Squad, stringResource(Res.string.tab_squad)),
    BridgeTab(BridgeIcon.Club, stringResource(Res.string.tab_club)),
)
