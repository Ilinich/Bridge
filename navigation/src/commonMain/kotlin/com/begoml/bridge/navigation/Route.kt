package com.begoml.bridge.navigation

import androidx.navigation3.runtime.NavKey

/**
 * A destination.
 *
 * Concrete routes live in the api module of the feature that owns them; this interface is the
 * vocabulary the host needs and the only routing type a feature has to know about.
 */
interface Route : NavKey {

    /** Stable text form, used to rebuild the back stack after a process death. */
    val key: String
}
