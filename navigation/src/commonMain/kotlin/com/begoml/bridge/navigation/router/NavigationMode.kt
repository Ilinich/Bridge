package com.begoml.bridge.navigation.router

/** How a destination behaves when it is already somewhere in the stack. */
enum class NavigationMode {

    /** Open it, whatever is already there. */
    Default,

    /** Open it only if the stack does not hold it anywhere. */
    OnlyIfNotInStack,

    /** Open it unless the user is already looking at it. */
    AvoidIfLastInStack,

    /**
     * Replace the top entry when it is the same kind of screen.
     *
     * Keeps a deep link or a notification from stacking a second copy of a screen the user is
     * already on, while still updating what it shows.
     */
    ReplaceIfSameTypeOnTop,
}
