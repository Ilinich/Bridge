package com.begoml.bridge.feature.club

import com.begoml.bridge.foundation.tessera.ScreenComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

/** What a Swift screen holds: the state holder, its state under a concrete type, and an end. */
class ClubComponent internal constructor(
    scope: CoroutineScope,
    val viewModel: ClubViewModel,
) : ScreenComponent(scope) {

    val state: StateFlow<ClubUiState> = viewModel.uiStateFlow
}
