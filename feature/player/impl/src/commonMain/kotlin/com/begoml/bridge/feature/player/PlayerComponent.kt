package com.begoml.bridge.feature.player

import com.begoml.bridge.foundation.tessera.ScreenComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

/** What a Swift screen holds: the state holder, its state under a concrete type, and an end. */
class PlayerComponent internal constructor(
    scope: CoroutineScope,
    val viewModel: PlayerViewModel,
) : ScreenComponent(scope) {

    val state: StateFlow<PlayerUiState> = viewModel.uiStateFlow
}
