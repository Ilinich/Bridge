package com.begoml.bridge.navigation

/**
 * Turns a route back into itself after a process death.
 *
 * A bundle carries primitives, so a saved back stack is a list of strings; each feature knows how
 * to read its own and returns null for anything else.
 */
interface RouteCodec {

    fun decode(key: String): Route?
}
