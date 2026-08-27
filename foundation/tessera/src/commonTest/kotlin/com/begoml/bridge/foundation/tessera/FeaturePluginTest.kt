package com.begoml.bridge.foundation.tessera

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

private data class CountState(val value: Int = 0)

private sealed interface CountAction {
    data object Increment : CountAction
}

private sealed interface CountEvent {
    data object Done : CountEvent
}

private class RecordingPlugin : FeaturePlugin<CountState, CountAction, CountEvent> {

    val transitions = mutableListOf<Pair<Int, Int>>()
    val actions = mutableListOf<CountAction>()
    val events = mutableListOf<CountEvent>()
    var started = 0

    override fun onStart() {
        started++
    }

    override fun onAction(action: CountAction) {
        actions += action
    }

    override fun onState(old: CountState, new: CountState) {
        transitions += old.value to new.value
    }

    override fun onEvent(event: CountEvent) {
        events += event
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class FeaturePluginTest {

    @Test
    fun `a plugin sees every transition that changed the state`() = runTest {
        val plugin = RecordingPlugin()
        val feature = feature<CountState, CountAction, CountEvent>(
            initialState = CountState(),
            scope = TestScope(testScheduler),
            plugins = listOf(plugin),
        )

        feature.updateState { CountState(1) }
        feature.updateState { CountState(2) }

        assertEquals(listOf(0 to 1, 1 to 2), plugin.transitions)
    }

    @Test
    fun `a transition that changes nothing is not reported`() = runTest {
        val plugin = RecordingPlugin()
        val feature = feature<CountState, CountAction, CountEvent>(
            initialState = CountState(7),
            scope = TestScope(testScheduler),
            plugins = listOf(plugin),
        )

        feature.updateState { it }
        feature.updateStateAsync { CountState(7) }

        assertEquals(emptyList<Pair<Int, Int>>(), plugin.transitions)
    }

    @Test
    fun `actions and events reach the plugin, and start is reported once`() = runTest {
        val plugin = RecordingPlugin()
        val feature = feature<CountState, CountAction, CountEvent>(
            initialState = CountState(),
            scope = TestScope(testScheduler),
            plugins = listOf(plugin),
        )

        feature.emitAction(CountAction.Increment)
        feature.emitEvent(CountEvent.Done)

        assertEquals(listOf<CountAction>(CountAction.Increment), plugin.actions)
        assertEquals(listOf<CountEvent>(CountEvent.Done), plugin.events)
        assertEquals(1, plugin.started)
    }
}
