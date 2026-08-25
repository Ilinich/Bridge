package com.begoml.bridge.foundation.tessera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Composable
fun <FeatureState> Feature<FeatureState, *, *, *>.collectState(
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
): State<FeatureState> = stateFlow.collectAsStateWithLifecycle(minActiveState = minActiveState)

@Composable
fun <UiState> UiStateDelegate<UiState, *>.collectUiState(
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
): State<UiState> = uiStateFlow.collectAsStateWithLifecycle(minActiveState = minActiveState)

/**
 * Handles one-shot events while the screen is at least [minActiveState].
 *
 * The default is RESUMED rather than STARTED so a navigation event cannot fire from a screen that
 * is already sliding away, which would push a destination onto the wrong back-stack entry.
 *
 * The gate is built from `Lifecycle.currentStateFlow` because `flowWithLifecycle` and
 * `repeatOnLifecycle` are Android-only; `collectLatest` supplies the cancel-and-restart part.
 * As with the Android helpers, an event taken from the channel exactly as the screen leaves
 * RESUMED is dropped — events are at-most-once by construction, so anything that must survive
 * belongs in state instead.
 */
@Composable
fun <Event> Feature<*, *, Event, *>.CollectEventEffect(
    vararg keys: Any?,
    minActiveState: Lifecycle.State = Lifecycle.State.RESUMED,
    onEvent: suspend (Event) -> Unit,
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(this, lifecycle, minActiveState, *keys) {
        lifecycle.currentStateFlow
            .map { state -> state.isAtLeast(minActiveState) }
            .distinctUntilChanged()
            .collectLatest { active -> if (active) events.collect(onEvent) }
    }
}
