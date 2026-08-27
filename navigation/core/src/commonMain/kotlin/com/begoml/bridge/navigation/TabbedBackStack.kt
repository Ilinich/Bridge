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
    private val roots: List<Route>,
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

    /** Every tab's stack, so a host can keep them all composed instead of swapping one in. */
    val allStacks: List<SnapshotStateList<Route>> get() = stacks

    /** Pops the given tab rather than whichever is selected; a page owns its own back. */
    fun popTab(index: Int) {
        val stack = stacks.getOrNull(index) ?: return
        if (stack.size > 1) stack.removeAt(stack.lastIndex)
    }

    val canPop: Boolean get() = current.size > 1

    fun push(route: Route) {
        if (current.lastOrNull() != route) current.add(route)
    }

    fun pop() {
        if (canPop) current.removeAt(current.lastIndex)
    }


    
    fun contains(route: Route): Boolean = current.contains(route)

    /**
     * Selects the tab a root destination belongs to.
     *
     * Returns false for anything that is not a root, which is how the router knows to push instead.
     */
    fun selectTabFor(route: Route): Boolean {
        val index = roots.indexOfFirst { it == route }
        if (index < 0) return false
        selectedTab = index
        return true
    }

    /** Selecting the tab already shown returns it to its root, the way a tab bar is expected to. */
    fun selectTab(index: Int) {
        if (index == selectedTab) {
            while (canPop) pop()
        } else {
            selectedTab = index
        }
    }

    internal fun snapshot(): List<List<String>> = stacks.map { stack -> stack.map { it.key } }
}

private fun tabbedBackStackSaver(
    roots: List<Route>,
    codecs: List<RouteCodec>,
): Saver<TabbedBackStack, Any> =
    listSaver(
        save = { stack -> listOf(stack.selectedTab) + stack.snapshot() },
        restore = { saved ->
            @Suppress("UNCHECKED_CAST")
            val stacks = saved.drop(1).map { raw ->
                (raw as List<String>).mapNotNull { key ->
                    codecs.firstNotNullOfOrNull { codec -> codec.decode(key) }
                }
            }
            TabbedBackStack(
                roots = roots,
                initialTab = saved.firstOrNull() as? Int ?: 0,
                initialStacks = stacks,
            )
        },
    )

@Composable
fun rememberTabbedBackStack(
    roots: List<Route>,
    codecs: List<RouteCodec>,
): TabbedBackStack =
    rememberSaveable(saver = tabbedBackStackSaver(roots, codecs)) { TabbedBackStack(roots) }
