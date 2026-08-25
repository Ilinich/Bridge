package com.begoml.bridge.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList

/**
 * One back stack per tab.
 *
 * Switching tabs does not reset where the user was: each tab keeps its own stack, so leaving the
 * squad open, checking the calendar and coming back returns to the same player.
 */
class TabbedBackStack internal constructor(roots: List<Route>) {

    private val stacks: List<SnapshotStateList<Route>> = roots.map { listOf(it).toMutableStateList() }

    var selectedTab: Int by mutableIntStateOf(0)
        private set

    val current: SnapshotStateList<Route> get() = stacks[selectedTab]

    val canPop: Boolean get() = current.size > 1

    fun push(route: Route) {
        if (current.lastOrNull() != route) current.add(route)
    }

    fun pop() {
        if (canPop) current.removeAt(current.lastIndex)
    }

    /** Selecting the tab already shown returns it to its root, the way a tab bar is expected to. */
    fun selectTab(index: Int) {
        if (index == selectedTab) {
            while (canPop) pop()
        } else {
            selectedTab = index
        }
    }
}

@Composable
fun rememberTabbedBackStack(roots: List<Route> = TabRoots): TabbedBackStack =
    remember { TabbedBackStack(roots) }
