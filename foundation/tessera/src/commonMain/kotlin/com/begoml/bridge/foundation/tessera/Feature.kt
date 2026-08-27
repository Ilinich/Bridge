package com.begoml.bridge.foundation.tessera

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
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

    suspend fun emitAction(action: Action)

    suspend fun awaitActions(onAction: suspend (Action) -> Unit)
}

/** One-shot output — navigation, a toast, a dialog. Never state. */
interface FeatureEventDelegate<Event> {

    val events: Flow<Event>

    fun dispatchEvent(event: Event)

    suspend fun emitEvent(event: Event)
}

/**
 * State a feature keeps for itself.
 *
 * It drives decisions but is never rendered, so changing it costs no recomposition. Anything the
 * UI reads belongs in [FeatureStateDelegate] instead.
 */
interface FeatureInternalStateDelegate<InternalState> {

    val internalState: InternalState

    suspend fun updateInternalState(transform: (InternalState) -> InternalState)
}

interface Feature<State, Action, Event, InternalState> :
    FeatureStateDelegate<State>,
    FeatureActionDelegate<Action>,
    FeatureEventDelegate<Event>,
    FeatureInternalStateDelegate<InternalState> {

    suspend fun updateState(transform: (State) -> State)

    fun updateStateAsync(transform: (State) -> State)
}

typealias SimpleFeature<State, Action, Event> = Feature<State, Action, Event, Unit>

/**
 * The default state holder: a state flow, an action channel and an event flow, mixed into a class
 * with `by feature(...)`.
 *
 * [updateState] serialises transforms behind a mutex, so a read-modify-write from two coroutines
 * cannot interleave. [updateStateAsync] is the non-suspending counterpart and relies on the
 * compare-and-set inside `MutableStateFlow.update`; it is correct for transforms that read only
 * the state passed in, and wrong for anything that also reads [internalState].
 */
fun <State, Action, Event> feature(
    initialState: State,
    scope: CoroutineScope,
): SimpleFeature<State, Action, Event> = FeatureImpl(initialState, Unit, scope)

private class FeatureImpl<State, Action, Event, InternalState>(
    initialState: State,
    initialInternalState: InternalState,
    private val scope: CoroutineScope,
) : Feature<State, Action, Event, InternalState> {

    private val mutableState = MutableStateFlow(initialState)
    private val mutableInternalState = MutableStateFlow(initialInternalState)
    private val stateMutex = Mutex()
    private val internalStateMutex = Mutex()
    private val actions = Channel<Action>(BUFFERED)
    private val eventChannel = Channel<Event>(BUFFERED)

    override val stateFlow: StateFlow<State> = mutableState.asStateFlow()

    override val internalState: InternalState get() = mutableInternalState.value

    override val events: Flow<Event> = eventChannel.receiveAsFlow()

    override suspend fun updateState(transform: (State) -> State) {
        // update() rather than a plain read-modify-write: updateStateAsync writes without taking
        // this mutex, so a value read here could otherwise be stale by the time it is written back
        // and the async write would be lost.
        stateMutex.withLock { mutableState.update(transform) }
    }

    override fun updateStateAsync(transform: (State) -> State) {
        mutableState.update(transform)
    }

    override suspend fun updateInternalState(transform: (InternalState) -> InternalState) {
        internalStateMutex.withLock {
            mutableInternalState.value = transform(mutableInternalState.value)
        }
    }

    override fun dispatchAction(action: Action) {
        scope.launch { actions.send(action) }
    }

    override suspend fun emitAction(action: Action) {
        actions.send(action)
    }

    override suspend fun awaitActions(onAction: suspend (Action) -> Unit) {
        for (action in actions) onAction(action)
    }

    override fun dispatchEvent(event: Event) {
        scope.launch { eventChannel.send(event) }
    }

    override suspend fun emitEvent(event: Event) {
        eventChannel.send(event)
    }
}
