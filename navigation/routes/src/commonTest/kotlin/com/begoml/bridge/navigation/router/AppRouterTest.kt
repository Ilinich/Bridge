package com.begoml.bridge.navigation.router

import com.begoml.bridge.navigation.Route
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private data object Somewhere : Route {
    override val key: String = "somewhere"
}

private data object Elsewhere : Route {
    override val key: String = "elsewhere"
}

@OptIn(ExperimentalCoroutinesApi::class)
class AppRouterTest {

    @Test
    fun repeats_what_was_asked_for_in_order() = runTest {
        val router: AppRouter = AppRouterImpl()
        val heard = mutableListOf<NavigationCommand>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            router.commands.toList(heard)
        }

        router.navigateTo(Somewhere)
        router.navigateUp()
        router.navigateTo(Elsewhere)
        runCurrent()

        assertEquals(
            listOf(
                NavigationCommand.NavigateTo(Somewhere),
                NavigationCommand.NavigateUp,
                NavigationCommand.NavigateTo(Elsewhere),
            ),
            heard,
        )
    }

    /**
     * The contract, stated as a test because it is the surprising half: navigation is not a queue.
     * Nobody listening means nobody moves — a command is a decision about *now*, and obeying it
     * later would move a user who has since gone somewhere else.
     */
    @Test
    fun a_decision_nobody_is_listening_to_is_dropped() = runTest {
        val router: AppRouter = AppRouterImpl()
        router.navigateTo(Somewhere)

        val heard = mutableListOf<NavigationCommand>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            router.commands.toList(heard)
        }
        runCurrent()

        assertTrue(heard.isEmpty())
    }
}
