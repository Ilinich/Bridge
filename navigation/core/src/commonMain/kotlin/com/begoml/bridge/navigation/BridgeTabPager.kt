package com.begoml.bridge.navigation

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier

/**
 * Holds every tab composed at once, one navigation surface per tab.
 *
 * A single surface fed the selected tab's stack instead would hand NavDisplay a different set of
 * keys on every switch, and its decorators clear the ViewModel store and saved state of keys that
 * are no longer in the stack: a scrolled list came back at the top and its ViewModel was rebuilt.
 * Pages are kept beyond the viewport so leaving a tab costs nothing.
 *
 * The pager does not scroll by touch. Pushed screens own the horizontal drag for swipe-back, and a
 * pager reading the same gesture would take it from them.
 */
@Composable
fun BridgeTabPager(
    backStack: TabbedBackStack,
    entries: List<FeatureNavigationEntry>,
    modifier: Modifier = Modifier,
) {
    val stacks = backStack.allStacks
    val pagerState = rememberPagerState(initialPage = backStack.selectedTab) { stacks.size }

    LaunchedEffect(pagerState, backStack) {
        snapshotFlow { backStack.selectedTab }.collect { index ->
            if (pagerState.currentPage != index) pagerState.scrollToPage(index)
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = modifier,
        userScrollEnabled = false,
        beyondViewportPageCount = stacks.size,
    ) { page ->
        BridgeNavDisplay(
            backStack = stacks[page],
            onBack = { backStack.popTab(page) },
            entries = entries,
            modifier = Modifier,
        )
    }
}
