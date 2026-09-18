package com.begoml.bridge.navigation

/**
 * A destination.
 *
 * Concrete routes live in the api module of the feature that owns them; this interface is the
 * vocabulary the host needs and the only routing type a feature has to know about.
 *
 * Plain Kotlin on purpose. A route says where to go, which is a decision; carrying a marker from
 * a navigation library would make that decision unrepeatable anywhere the library is absent.
 */
interface Route {

    /** Stable text form, used to rebuild the back stack after a process death. */
    val key: String
}
