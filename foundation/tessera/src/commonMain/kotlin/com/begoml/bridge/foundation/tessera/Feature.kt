package com.begoml.bridge.foundation.tessera

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Rendered state. Must be an immutable data class so Compose can skip on equality. */
interface FeatureStateDelegate<State> {

    val stateFlow: StateFlow<State>

    val state: State get() = stateFlow.value
}

/** Intent arriving from the UI, consumed by exactly one collector. */
interface FeatureActionDelegate<Action> {

    fun dispatchAction(action: Action)

    suspend fun awaitActions(onAction: suspend (Action) -> Unit)
}

/** One-shot output — navigation, a toast, a dialog. Never state. */
interface FeatureEventDelegate<Event> {

    val events: Flow<Event>

    fun dispatchEvent(event: Event)
}

interface Feature<State, Action, Event> :
    FeatureStateDelegate<State>,
    FeatureActionDelegate<Action>,
    FeatureEventDelegate<Event> {

    suspend fun updateState(transform: (State) -> State)
}

/**
 * A listener on a feature's traffic, installed at construction.
 *
 * It **observes** and does not intervene: no hook can rewrite an action or veto a state. That is
 * the line between a plugin and the feature's own logic — a plugin that can swallow an action
 * moves behaviour out of the feature and into the wiring, where nobody looks for it. Logging,
 * analytics and debug panels all fit on this side of the line.
 *
 * Hooks run on whichever coroutine caused them, so they must be cheap and must not throw.
 */
interface FeaturePlugin<State, Action, Event> {

    fun onStart() = Unit

    fun onAction(action: Action) = Unit

    /** Only for a transition that changed something; equal states are not reported. */
    fun onState(old: State, new: State) = Unit

    fun onEvent(event: Event) = Unit

    /** The scope that owned the feature has ended; [error] is null for a plain cancellation. */
    fun onStop(error: Throwable?) = Unit
}

/**
 * The default state holder: a state flow, an action channel and an event flow, mixed into a class
 * with `by feature(...)`.
 *
 * [updateState] serialises transforms behind a mutex, so a read-modify-write from two coroutines
 * cannot interleave.
 */
fun <State, Action, Event> feature(
    initialState: State,
    scope: CoroutineScope,
    plugins: List<FeaturePlugin<State, Action, Event>> = emptyList(),
): Feature<State, Action, Event> = FeatureImpl(initialState, scope, plugins)

private class FeatureImpl<State, Action, Event>(
    initialState: State,
    private val scope: CoroutineScope,
    private val plugins: List<FeaturePlugin<State, Action, Event>> = emptyList(),
) : Feature<State, Action, Event> {

    private val mutableState = MutableStateFlow(initialState)
    private val stateMutex = Mutex()
    private val actions = Channel<Action>(BUFFERED)
    private val eventChannel = Channel<Event>(BUFFERED)

    override val stateFlow: StateFlow<State> = mutableState.asStateFlow()

    override val events: Flow<Event> = eventChannel.receiveAsFlow()

    init {
        plugins.forEach { plugin -> plugin.onStart() }
        scope.coroutineContext[Job]?.invokeOnCompletion { error ->
            plugins.forEach { plugin -> plugin.onStop(error) }
        }
    }

    override suspend fun updateState(transform: (State) -> State) {
        stateMutex.withLock { commit(transform) }
    }

    /**
     * The one place state is written, so plugins see every transition and see it once.
     *
     * The compare-and-set loop is what `MutableStateFlow.update` does; it is spelled out because a
     * plugin needs the value that was replaced, and `update` does not hand it back.
     */
    private fun commit(transform: (State) -> State) {
        var old: State
        var new: State
        do {
            old = mutableState.value
            new = transform(old)
        } while (!mutableState.compareAndSet(old, new))
        if (old != new) plugins.forEach { plugin -> plugin.onState(old, new) }
    }

    override fun dispatchAction(action: Action) {
        scope.launch { emitAction(action) }
    }

    private suspend fun emitAction(action: Action) {
        plugins.forEach { plugin -> plugin.onAction(action) }
        actions.send(action)
    }

    override suspend fun awaitActions(onAction: suspend (Action) -> Unit) {
        for (action in actions) onAction(action)
    }

    override fun dispatchEvent(event: Event) {
        scope.launch { emitEvent(event) }
    }

    private suspend fun emitEvent(event: Event) {
        plugins.forEach { plugin -> plugin.onEvent(event) }
        eventChannel.send(event)
    }
}
