package com.begoml.bridge.feature.squad

import com.begoml.bridge.foundation.tessera.ScreenComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

/** What a Swift screen holds: the state holder, its state under a concrete type, and an end. */
class SquadComponent internal constructor(
    scope: CoroutineScope,
    val viewModel: SquadViewModel,
) : ScreenComponent(scope) {

    val state: StateFlow<SquadUiState> = viewModel.uiStateFlow
}
