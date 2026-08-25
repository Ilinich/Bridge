package com.begoml.bridge.navigation

import androidx.navigation3.runtime.NavKey

sealed interface Route : NavKey {

    data object Matchday : Route

    data object Season : Route

    data object Squad : Route

    data object Club : Route

    data class MatchDetail(val matchId: String) : Route

    data class PlayerDetail(val playerId: String) : Route
}

/** The tab a route belongs to, and the roots the tab bar switches between. */
val TabRoots: List<Route> = listOf(Route.Matchday, Route.Season, Route.Squad, Route.Club)
