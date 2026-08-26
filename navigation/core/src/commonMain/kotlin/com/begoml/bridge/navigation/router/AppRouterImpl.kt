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
class AppRouterImpl : AppRouter {

    private var backStack: TabbedBackStack? = null

    fun attach(stack: TabbedBackStack) {
        backStack = stack
    }

    fun detach() {
        backStack = null
    }

    override fun executeCommands(vararg commands: NavigationCommand) {
        val stack = backStack ?: return
        commands.forEach { command -> stack.apply(command) }
    }

    private fun TabbedBackStack.apply(command: NavigationCommand) {
        when (command) {
            is NavigationCommand.NavigateTo -> navigate(command.destination, command.mode)
            is NavigationCommand.NavigateUp -> pop()
            is NavigationCommand.NavigateUpTo -> popTo(command.destination)
            is NavigationCommand.ReplaceWith -> {
                pop()
                push(command.destination)
            }

            is NavigationCommand.ClearBackStackAndNavigateTo -> {
                popToRoot()
                navigate(command.destination, NavigationMode.Default)
            }
        }
    }

    private fun TabbedBackStack.navigate(destination: Route, mode: NavigationMode) {
        if (selectTabFor(destination)) return

        when (mode) {
            NavigationMode.Default -> push(destination)
            NavigationMode.OnlyIfNotInStack -> if (!contains(destination)) push(destination)
            NavigationMode.AvoidIfLastInStack -> if (current.lastOrNull() != destination) {
                push(destination)
            }

            NavigationMode.ReplaceIfSameTypeOnTop -> {
                if (current.lastOrNull()?.isSameKindAs(destination) == true) pop()
                push(destination)
            }
        }
    }
}

private fun Route.isSameKindAs(other: Route): Boolean = this::class == other::class
