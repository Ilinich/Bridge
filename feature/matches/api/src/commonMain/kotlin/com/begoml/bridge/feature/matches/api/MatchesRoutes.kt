package com.begoml.bridge.feature.matches.api

import com.begoml.bridge.navigation.Route
import com.begoml.bridge.navigation.RouteCodec

private const val MatchPrefix = "match:"

data object MatchdayRoute : Route {
    override val key: String = "matchday"
}

data object SeasonRoute : Route {
    override val key: String = "season"
}

data class MatchDetailRoute(val matchId: String) : Route {
    override val key: String = MatchPrefix + matchId
}

class MatchesRouteCodec : RouteCodec {

    override fun decode(key: String): Route? = when {
        key == MatchdayRoute.key -> MatchdayRoute
        key == SeasonRoute.key -> SeasonRoute
        key.startsWith(MatchPrefix) -> MatchDetailRoute(key.removePrefix(MatchPrefix))
        else -> null
    }
}
