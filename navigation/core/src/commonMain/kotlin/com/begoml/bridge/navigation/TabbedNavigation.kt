package com.begoml.bridge.navigation

import com.begoml.bridge.navigation.router.NavigationCommand

/**
 * Applies one decision to the tabbed stack this host owns.
 *
 * The adaptation the tabs force lives here: a destination that **is** a tab root selects that tab
 * instead of being pushed onto the current one. Pushing a tab root would bury a whole section
 * inside another section's history, and the back gesture would then walk out of a tab rather than
 * up it. The Swift host makes the same decision against its own stacks.
 */
fun TabbedBackStack.apply(command: NavigationCommand) {
    when (command) {
        is NavigationCommand.NavigateTo -> if (!selectTabFor(command.destination)) {
            push(command.destination)
        }
        is NavigationCommand.NavigateUp -> pop()
    }
}
