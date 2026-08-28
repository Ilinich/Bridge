package com.begoml.bridge.foundation.tessera

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * The simpler state holder: one state flow, no actions and no composition.
 *
 * Reading is public; **writing is not**. [updateUiState] and [asyncUpdateUiState] are
 * declared as member extensions on the delegate, so they resolve only inside the class that mixes
 * the delegate in with `by`. A screen that receives the holder can observe it and cannot mutate it
 * — the encapsulation is enforced by resolution rules rather than by convention.
 *
 * Reach for [feature] instead when state has to be folded from several sources.
 */
interface UiStateDelegate<UiState> {

    val uiStateFlow: StateFlow<UiState>

    val UiStateDelegate<UiState>.uiState: UiState

    suspend fun UiStateDelegate<UiState>.updateUiState(transform: (UiState) -> UiState)

    fun UiStateDelegate<UiState>.asyncUpdateUiState(
        scope: CoroutineScope,
        transform: (UiState) -> UiState,
    ): Job

}

class UiStateDelegateImpl<UiState>(
    initialUiState: UiState,
) : UiStateDelegate<UiState> {

    private val mutableUiState = MutableStateFlow(initialUiState)
    private val stateMutex = Mutex()

    override val uiStateFlow: StateFlow<UiState> = mutableUiState.asStateFlow()

    override val UiStateDelegate<UiState>.uiState: UiState
        get() = mutableUiState.value

    override suspend fun UiStateDelegate<UiState>.updateUiState(
        transform: (UiState) -> UiState,
    ) {
        stateMutex.withLock { mutableUiState.value = transform(mutableUiState.value) }
    }

    override fun UiStateDelegate<UiState>.asyncUpdateUiState(
        scope: CoroutineScope,
        transform: (UiState) -> UiState,
    ): Job = scope.launch { updateUiState(transform) }

}
