package com.begoml.bridge.feature.squad.api

import com.begoml.bridge.navigation.Route
import com.begoml.bridge.navigation.RouteCodec

private const val PlayerPrefix = "player:"

data object SquadRoute : Route {
    override val key: String = "squad"
}

data class PlayerDetailRoute(val playerId: String) : Route {
    override val key: String = PlayerPrefix + playerId
}

class SquadRouteCodec : RouteCodec {

    override fun decode(key: String): Route? = when {
        key == SquadRoute.key -> SquadRoute
        key.startsWith(PlayerPrefix) -> PlayerDetailRoute(key.removePrefix(PlayerPrefix))
        else -> null
    }
}
