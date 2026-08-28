package com.begoml.bridge.feature.squad.api

import com.begoml.bridge.navigation.Route
import com.begoml.bridge.navigation.RouteCodec

data object SquadRoute : Route {
    override val key: String = "squad"
}

class SquadRouteCodec : RouteCodec {

    override fun decode(key: String): Route? = SquadRoute.takeIf { key == it.key }
}
