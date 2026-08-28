package com.begoml.bridge.feature.player.api

import com.begoml.bridge.navigation.Route
import com.begoml.bridge.navigation.RouteCodec

private const val PlayerPrefix = "player:"

data class PlayerDetailRoute(val playerId: String) : Route {
    override val key: String = PlayerPrefix + playerId
}

class PlayerRouteCodec : RouteCodec {

    override fun decode(key: String): Route? = when {
        key.startsWith(PlayerPrefix) -> PlayerDetailRoute(key.removePrefix(PlayerPrefix))
        else -> null
    }
}
