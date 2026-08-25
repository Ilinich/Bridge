package com.begoml.bridge.foundation.tessera

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Folds other sources into this feature's state.
 *
 * Every overload takes [StateFlow], never a bare [Flow], and that is the whole point. `combine`
 * emits nothing until *every* source has produced a value, so one silent source — an endpoint that
 * returns an empty list, a repository that has not loaded yet — leaves the screen blank forever,
 * with no error to show for it. A [StateFlow] always holds a value, which makes that failure
 * unrepresentable.
 *
 * To fold a plain [Flow], seed it with [withInitial] first; the seed is the explicit answer to
 * "what does this screen show before the source speaks".
 */
fun <T, State> Feature<State, *, *, *>.composeState(
    scope: CoroutineScope,
    source: StateFlow<T>,
    transform: (State, T) -> State,
): Job = scope.launch {
    source.collectLatest { value -> updateState { state -> transform(state, value) } }
}

fun <T1, T2, State> Feature<State, *, *, *>.composeState(
    scope: CoroutineScope,
    source1: StateFlow<T1>,
    source2: StateFlow<T2>,
    transform: (State, T1, T2) -> State,
): Job = scope.launch {
    combine(source1, source2) { value1, value2 -> value1 to value2 }
        .collectLatest { (value1, value2) ->
            updateState { state -> transform(state, value1, value2) }
        }
}

fun <T1, T2, T3, State> Feature<State, *, *, *>.composeState(
    scope: CoroutineScope,
    source1: StateFlow<T1>,
    source2: StateFlow<T2>,
    source3: StateFlow<T3>,
    transform: (State, T1, T2, T3) -> State,
): Job = scope.launch {
    combine(source1, source2, source3) { value1, value2, value3 ->
        Triple(value1, value2, value3)
    }.collectLatest { (value1, value2, value3) ->
        updateState { state -> transform(state, value1, value2, value3) }
    }
}

/** Gives a cold [Flow] a value to start from, so it can take part in [composeState]. */
fun <T> Flow<T>.withInitial(scope: CoroutineScope, initial: T): StateFlow<T> =
    stateIn(scope, SharingStarted.Eagerly, initial)
