package com.begoml.bridge.feature.matches.season

import com.begoml.bridge.foundation.tessera.ScreenComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

/** What a Swift screen holds: the state holder, its state under a concrete type, and an end. */
class SeasonComponent internal constructor(
    scope: CoroutineScope,
    val viewModel: SeasonViewModel,
) : ScreenComponent(scope) {

    val state: StateFlow<SeasonUiState> = viewModel.uiStateFlow
}
