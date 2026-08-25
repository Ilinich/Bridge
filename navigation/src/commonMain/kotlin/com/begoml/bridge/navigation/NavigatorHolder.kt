package com.begoml.bridge.navigation

/**
 * The navigator a feature injects, standing in for the one composition owns.
 *
 * The back stack has to be saveable, which means it lives in the composition and is rebuilt after
 * a process death; features resolve their navigator once, from the graph. This holder bridges the
 * two lifetimes: the composition points it at the current stack, and a call that arrives while
 * nothing is attached is dropped rather than crashing.
 */
class NavigatorHolder : Navigator {

    private var delegate: Navigator? = null

    fun attach(navigator: Navigator) {
        delegate = navigator
    }

    fun detach() {
        delegate = null
    }

    override fun push(route: Route) {
        delegate?.push(route)
    }

    override fun pop() {
        delegate?.pop()
    }
}
