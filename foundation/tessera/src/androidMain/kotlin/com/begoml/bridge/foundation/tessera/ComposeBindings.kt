package com.begoml.bridge.foundation.tessera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Reads a holder's state, and stops reading while the screen is below [minActiveState].
 *
 * Every screen goes through this rather than through `collectAsStateWithLifecycle` directly, so
 * "what does a screen observe, and when does it stop" is answered in one place.
 */
@Composable
fun <UiState> UiStateDelegate<UiState>.collectUiState(
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
): State<UiState> = uiStateFlow.collectAsStateWithLifecycle(minActiveState = minActiveState)
