package com.begoml.bridge.navigation

/**
 * What a feature is allowed to ask of navigation.
 *
 * A feature pushes and pops; it never learns which tab it is in, what else is on the stack, or
 * that tabs exist at all.
 */
interface Navigator {

    fun push(route: Route)

    fun pop()
}
