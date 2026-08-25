package com.begoml.bridge.foundation.tessera

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * The simpler state holder: one state flow and one event channel, no actions and no composition.
 *
 * Reading is public; **writing is not**. [updateUiState], [asyncUpdateUiState] and [sendEvent] are
 * declared as member extensions on the delegate, so they resolve only inside the class that mixes
 * the delegate in with `by`. A screen that receives the holder can observe it and cannot mutate it
 * — the encapsulation is enforced by resolution rules rather than by convention.
 *
 * Reach for [feature] instead when state has to be folded from several sources.
 */
interface UiStateDelegate<UiState, Event> {

    val uiStateFlow: StateFlow<UiState>

    val singleEvents: Flow<Event>

    val UiStateDelegate<UiState, Event>.uiState: UiState

    suspend fun UiStateDelegate<UiState, Event>.updateUiState(transform: (UiState) -> UiState)

    fun UiStateDelegate<UiState, Event>.asyncUpdateUiState(
        scope: CoroutineScope,
        transform: (UiState) -> UiState,
    ): Job

    suspend fun UiStateDelegate<UiState, Event>.sendEvent(event: Event)
}

class UiStateDelegateImpl<UiState, Event>(
    initialUiState: UiState,
    capacity: Int = BUFFERED,
) : UiStateDelegate<UiState, Event> {

    private val mutableUiState = MutableStateFlow(initialUiState)
    private val eventChannel = Channel<Event>(capacity)
    private val stateMutex = Mutex()

    override val uiStateFlow: StateFlow<UiState> = mutableUiState.asStateFlow()

    override val singleEvents: Flow<Event> = eventChannel.receiveAsFlow()

    override val UiStateDelegate<UiState, Event>.uiState: UiState
        get() = mutableUiState.value

    override suspend fun UiStateDelegate<UiState, Event>.updateUiState(
        transform: (UiState) -> UiState,
    ) {
        stateMutex.withLock { mutableUiState.value = transform(mutableUiState.value) }
    }

    override fun UiStateDelegate<UiState, Event>.asyncUpdateUiState(
        scope: CoroutineScope,
        transform: (UiState) -> UiState,
    ): Job = scope.launch { updateUiState(transform) }

    override suspend fun UiStateDelegate<UiState, Event>.sendEvent(event: Event) {
        eventChannel.send(event)
    }
}
