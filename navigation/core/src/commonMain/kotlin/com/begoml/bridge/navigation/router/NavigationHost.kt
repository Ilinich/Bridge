package com.begoml.bridge.navigation.router

import com.begoml.bridge.navigation.TabbedBackStack

/**
 * How the composition lends the router the stack it drives.
 *
 * Separate from [AppRouter] because the two have different audiences: a feature holds a router and
 * asks to go somewhere, while exactly one host owns the stack and its lifetime. Keeping them apart
 * is what lets the implementation stay internal and lets a test supply a router without owning a
 * back stack.
 */
interface NavigationHost {

    fun attach(stack: TabbedBackStack)

    fun detach()
}
