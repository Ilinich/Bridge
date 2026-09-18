package com.begoml.bridge.navigation.router

import com.begoml.bridge.navigation.Route
import kotlinx.coroutines.flow.Flow

/**
 * How a feature asks the app to go somewhere.
 *
 * A feature holds a destination — a type from another feature's `api` — and a router. It never
 * holds the other feature's screens, state holders or module, and it never learns which tab it is
 * in or what else is on the stack.
 *
 * What it asks for is a *decision*, not a move: the router says where the app should go, and the
 * host that owns the real back stack decides how that looks. There are two such hosts and they
 * agree on nothing below this line — one animates a Compose back stack, the other pushes onto a
 * UINavigationController — which is exactly why this contract stops here.
 */
interface AppRouter {

    /** The decisions, in order. A host collects them for as long as it is on screen. */
    val commands: Flow<NavigationCommand>

    fun executeCommands(vararg commands: NavigationCommand)
}

fun AppRouter.navigateTo(destination: Route) =
    executeCommands(NavigationCommand.NavigateTo(destination))

fun AppRouter.navigateUp() = executeCommands(NavigationCommand.NavigateUp)
