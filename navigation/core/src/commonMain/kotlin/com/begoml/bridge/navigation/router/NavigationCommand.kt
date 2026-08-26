package com.begoml.bridge.navigation.router

import com.begoml.bridge.navigation.Route

/** One instruction to the navigation host. */
sealed interface NavigationCommand {

    data class NavigateTo(
        val destination: Route,
        val mode: NavigationMode = NavigationMode.Default,
    ) : NavigationCommand

    data object NavigateUp : NavigationCommand

    data class NavigateUpTo(val destination: Route) : NavigationCommand

    data class ReplaceWith(val destination: Route) : NavigationCommand

    data class ClearBackStackAndNavigateTo(val destination: Route) : NavigationCommand
}
