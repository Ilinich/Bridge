package com.begoml.bridge.navigation.router

import com.begoml.bridge.navigation.Route

/**
 * How a feature asks the app to go somewhere.
 *
 * A feature holds a destination — a type from another feature's `api` — and a router. It never
 * holds the other feature's screens, state holders or module, and it never learns which tab it is
 * in or what else is on the stack.
 */
interface AppRouter {

    fun executeCommands(vararg commands: NavigationCommand)
}

fun AppRouter.navigateTo(destination: Route) =
    executeCommands(NavigationCommand.NavigateTo(destination))

/** Opens the destination unless the stack already holds it anywhere. */
fun AppRouter.navigateToIfNotInBackStack(destination: Route) = executeCommands(
    NavigationCommand.NavigateTo(destination, NavigationMode.OnlyIfNotInStack),
)

/** Opens the destination unless the user is already looking at it. */
fun AppRouter.navigateToIfNotLastInBackStack(destination: Route) = executeCommands(
    NavigationCommand.NavigateTo(destination, NavigationMode.AvoidIfLastInStack),
)

fun AppRouter.navigateOrReplaceIfSameTypeOnTop(destination: Route) = executeCommands(
    NavigationCommand.NavigateTo(destination, NavigationMode.ReplaceIfSameTypeOnTop),
)

fun AppRouter.replaceWith(destination: Route) =
    executeCommands(NavigationCommand.ReplaceWith(destination))

fun AppRouter.clearBackStackAndNavigateTo(destination: Route) =
    executeCommands(NavigationCommand.ClearBackStackAndNavigateTo(destination))

fun AppRouter.navigateUp() = executeCommands(NavigationCommand.NavigateUp)

fun AppRouter.navigateUpTo(destination: Route) =
    executeCommands(NavigationCommand.NavigateUpTo(destination))
