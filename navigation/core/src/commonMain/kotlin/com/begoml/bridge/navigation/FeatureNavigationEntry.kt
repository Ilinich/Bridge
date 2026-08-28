package com.begoml.bridge.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

/**
 * A feature's contribution to the navigation graph.
 *
 * Each feature registers its own destinations, so the host never learns what screens exist. Adding
 * a feature means adding a binding, not editing a `when` in the composition root that has to name
 * every screen and every state holder in the app.
 */
interface FeatureNavigationEntry {

    fun register(scope: EntryProviderScope<NavKey>)
}

/**
 * Turns a route back into itself after a process death.
 *
 * A bundle carries primitives, so a saved back stack is a list of strings; each feature knows how
 * to read its own and returns null for anything else.
 */
interface RouteCodec {

    fun decode(key: String): Route?
}
