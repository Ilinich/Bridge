package com.begoml.bridge.feature.club.api

import com.begoml.bridge.navigation.Route
import com.begoml.bridge.navigation.RouteCodec

/** The destinations this feature owns. Everything else about it stays inside `impl`. */
data object ClubRoute : Route {
    override val key: String = "club"
}

class ClubRouteCodec : RouteCodec {

    override fun decode(key: String): Route? = if (key == ClubRoute.key) ClubRoute else null
}
