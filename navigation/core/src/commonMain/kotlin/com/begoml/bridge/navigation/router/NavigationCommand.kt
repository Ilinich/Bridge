package com.begoml.bridge.navigation.router

import com.begoml.bridge.navigation.Route

/** One instruction to the navigation host. */
sealed interface NavigationCommand {

    data class NavigateTo(val destination: Route) : NavigationCommand

    data object NavigateUp : NavigationCommand
}
