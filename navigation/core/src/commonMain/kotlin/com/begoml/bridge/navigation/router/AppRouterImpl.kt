package com.begoml.bridge.navigation.router

import com.begoml.bridge.navigation.Route
import com.begoml.bridge.navigation.TabbedBackStack

/**
 * Runs navigation commands against the tabbed back stack.
 *
 * One adaptation the tabs force, and it is the interesting part: a destination that **is** a tab
 * root selects that tab instead of being pushed onto the current one. Pushing a tab root would
 * bury a whole section inside another section's history, and the back gesture would then walk out
 * of a tab rather than up it.
 *
 * A command that arrives while no stack is attached is dropped. The stack lives in composition so
 * it can be restored after a process death, while features resolve their router once from the
 * graph; the gap between those two lifetimes is here, in one place, rather than in every caller.
 */
internal class AppRouterImpl : AppRouter, NavigationHost {

    private var backStack: TabbedBackStack? = null

    override fun attach(stack: TabbedBackStack) {
        backStack = stack
    }

    override fun detach() {
        backStack = null
    }

    override fun executeCommands(vararg commands: NavigationCommand) {
        val stack = backStack ?: return
        commands.forEach { command -> stack.apply(command) }
    }

    private fun TabbedBackStack.apply(command: NavigationCommand) {
        when (command) {
            is NavigationCommand.NavigateTo -> navigate(command.destination)
            is NavigationCommand.NavigateUp -> pop()
        }
    }

    private fun TabbedBackStack.navigate(destination: Route) {
        if (selectTabFor(destination)) return
        push(destination)
    }
}
