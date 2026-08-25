package com.begoml.bridge.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList

/**
 * One back stack per tab, surviving configuration changes.
 *
 * Switching tabs does not reset where the user was: each tab keeps its own stack, so leaving the
 * squad open, checking the calendar and coming back returns to the same player.
 *
 * The whole thing is saveable, because on Android a rotation destroys the activity and a stack
 * held in `remember` would silently reset to its roots — the user would rotate the phone and find
 * themselves back on the first tab.
 */
class TabbedBackStack internal constructor(
    roots: List<Route>,
    initialTab: Int = 0,
    initialStacks: List<List<Route>>? = null,
) {

    private val stacks: List<SnapshotStateList<Route>> =
        roots.mapIndexed { index, root ->
            (initialStacks?.getOrNull(index)?.takeIf { it.isNotEmpty() } ?: listOf(root))
                .toMutableStateList()
        }

    var selectedTab: Int by mutableIntStateOf(initialTab.coerceIn(roots.indices))
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

    internal fun snapshot(): List<List<String>> = stacks.map { stack -> stack.map(Route::encode) }
}

/**
 * Routes are encoded as text rather than serialized.
 *
 * A bundle can only carry primitives, and the alternative — making every route `@Serializable` and
 * threading a format through — buys nothing for five destinations.
 */
private fun Route.encode(): String = when (this) {
    Route.Matchday -> "matchday"
    Route.Season -> "season"
    Route.Squad -> "squad"
    is Route.MatchDetail -> "match:$matchId"
    is Route.PlayerDetail -> "player:$playerId"
}

private fun decodeRoute(raw: String): Route? = when {
    raw == "matchday" -> Route.Matchday
    raw == "season" -> Route.Season
    raw == "squad" -> Route.Squad
    raw.startsWith("match:") -> Route.MatchDetail(raw.removePrefix("match:"))
    raw.startsWith("player:") -> Route.PlayerDetail(raw.removePrefix("player:"))
    else -> null
}

private fun tabbedBackStackSaver(roots: List<Route>): Saver<TabbedBackStack, Any> =
    listSaver(
        save = { stack -> listOf(stack.selectedTab) + stack.snapshot() },
        restore = { saved ->
            @Suppress("UNCHECKED_CAST")
            val stacks = saved.drop(1).map { raw ->
                (raw as List<String>).mapNotNull(::decodeRoute)
            }
            TabbedBackStack(
                roots = roots,
                initialTab = saved.firstOrNull() as? Int ?: 0,
                initialStacks = stacks,
            )
        },
    )

@Composable
fun rememberTabbedBackStack(roots: List<Route> = TabRoots): TabbedBackStack =
    rememberSaveable(saver = tabbedBackStackSaver(roots)) { TabbedBackStack(roots) }
