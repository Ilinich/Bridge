package com.begoml.bridge.host

import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.ImmutableList
import com.begoml.bridge.navigation.TabbedBackStack
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import com.begoml.bridge.feature.club.api.ClubRoute
import com.begoml.bridge.feature.matches.api.MatchdayRoute
import com.begoml.bridge.feature.matches.api.SeasonRoute
import com.begoml.bridge.feature.squad.api.SquadRoute
import com.begoml.bridge.core.analytics.Analytics
import com.begoml.bridge.foundation.logger.Logger
import com.begoml.bridge.foundation.logger.warn
import com.begoml.bridge.host.analytics.AppOpened
import com.begoml.bridge.host.analytics.TabSelected
import com.begoml.bridge.navigation.BridgeTabPager
import com.begoml.bridge.navigation.FeatureNavigationEntry
import com.begoml.bridge.navigation.apply
import com.begoml.bridge.navigation.router.AppRouter
import com.begoml.bridge.navigation.Route
import com.begoml.bridge.navigation.RouteCodec
import com.begoml.bridge.navigation.rememberTabbedBackStack
import com.begoml.bridge.navigation.swipe.LocalSwipeDismissSignal
import com.begoml.bridge.navigation.swipe.rememberSwipeDismissSignal
import com.begoml.bridge.uikit.LocalScreenPadding
import com.begoml.bridge.uikit.component.BridgeIcon
import com.begoml.bridge.uikit.component.BridgeTab
import com.begoml.bridge.uikit.component.BridgeTabBar
import com.begoml.bridge.uikit.glass.GlassBackdrop
import com.begoml.bridge.uikit.theme.BridgeColors
import com.begoml.bridge.uikit.theme.BridgeTheme
import com.begoml.bridge.HostStrings
import com.begoml.bridge.tab_club
import com.begoml.bridge.tab_matchday
import com.begoml.bridge.tab_season
import com.begoml.bridge.tab_squad
import dev.icerock.moko.resources.compose.stringResource
import org.koin.compose.currentKoinScope
import org.koin.compose.koinInject

/** Tab-bar height plus the gap above and below it; screens scroll behind all of it. */
private val TabBarInset = 70.dp
private const val CrossfadeMillis = 150
private const val NavigationTag = "Navigation"

private val TabRoutes: ImmutableList<Route> = persistentListOf(MatchdayRoute, SeasonRoute, SquadRoute, ClubRoute)

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
        val codecs = remember(koin) { koin.getAll<RouteCodec>().toImmutableList() }
        val entries = remember(koin) { koin.getAll<FeatureNavigationEntry>().toImmutableList() }
        val router: AppRouter = koinInject()
        val analytics: Analytics = koinInject()

        LaunchedEffect(analytics) { analytics.track(AppOpened) }

        val logger: Logger = koinInject()
        val backStack = rememberTabbedBackStack(
            roots = TabRoutes,
            codecs = codecs,
            onUnknownKey = { key -> logger.warn(NavigationTag, "no codec for the saved route $key") },
        )
        // The host applies the router's decisions to the stack it owns; the router itself owns
        // nothing, which is what lets a Swift host apply the same decisions to its own stacks.
        LaunchedEffect(backStack) {
            router.commands.collect { command -> backStack.apply(command) }
        }

        Shell(backStack = backStack, entries = entries, analytics = analytics)
    }
}

/**
 * The tabs, the bars and the host under them.
 *
 * Separate from [App] because the two answer different questions: [App] wires the back stack to
 * the router, this decides what the app looks like on top of it.
 */
@Composable
private fun Shell(
    backStack: TabbedBackStack,
    entries: ImmutableList<FeatureNavigationEntry>,
    analytics: Analytics,
) {
    val tabs = bridgeTabs()
    val contentPadding = screenPadding()

    // The bars are drawn over the navigation host, not inside it, so a screen sliding away under
    // them is invisible to anything scoped to that screen. The signal is provided here, above
    // both, and written from inside the host.
    val swipeSignal = rememberSwipeDismissSignal()

    CompositionLocalProvider(
        LocalScreenPadding provides contentPadding,
        LocalSwipeDismissSignal provides swipeSignal,
    ) {
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
                    analytics.track(TabSelected(tabs[index].label))
                    backStack.selectTab(index)
                },
            )
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
private fun bridgeTabs(): ImmutableList<BridgeTab> = persistentListOf(
    BridgeTab(BridgeIcon.Matchday, stringResource(HostStrings.strings.tab_matchday)),
    BridgeTab(BridgeIcon.Season, stringResource(HostStrings.strings.tab_season)),
    BridgeTab(BridgeIcon.Squad, stringResource(HostStrings.strings.tab_squad)),
    BridgeTab(BridgeIcon.Club, stringResource(HostStrings.strings.tab_club)),
)
