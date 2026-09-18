package com.begoml.bridge.navigation.router

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow

private const val PendingCommands = 8

/**
 * Holds nothing and moves nothing: it repeats what a feature asked for.
 *
 * Nothing is replayed. A decision made while no host is listening is dropped, because navigation
 * is about *now*: obeying it once a host appears would move a user who has since gone elsewhere —
 * and the previous implementation, which drove the stack directly, dropped it for the same reason.
 * The buffer only covers a host that is slow, not one that is absent, and it is small on purpose.
 */
internal class AppRouterImpl : AppRouter {

    private val mutableCommands = MutableSharedFlow<NavigationCommand>(
        extraBufferCapacity = PendingCommands,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override val commands = mutableCommands

    override fun executeCommands(vararg commands: NavigationCommand) {
        commands.forEach { command -> mutableCommands.tryEmit(command) }
    }
}
