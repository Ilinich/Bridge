package com.begoml.bridge.feature.matches.detail

import com.begoml.bridge.foundation.tessera.ScreenComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

/** What a Swift screen holds: the state holder, its state under a concrete type, and an end. */
class MatchDetailComponent internal constructor(
    scope: CoroutineScope,
    val viewModel: MatchDetailViewModel,
) : ScreenComponent(scope) {

    val state: StateFlow<MatchDetailUiState> = viewModel.uiStateFlow
}
