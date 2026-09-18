package com.begoml.bridge.feature.matches.matchday

import kotlinx.coroutines.CoroutineScope
import com.begoml.bridge.foundation.tessera.ScreenComponent
import kotlinx.coroutines.flow.StateFlow

/** What a Swift screen holds: the state holder, its state under a concrete type, and an end. */
class MatchdayComponent internal constructor(
    scope: CoroutineScope,
    val viewModel: MatchdayViewModel,
) : ScreenComponent(scope) {


    /**
     * The state, as a concrete type.
     *
     * The state holder exposes it through a generic interface, and a generic interface is the one
     * shape the Swift export handles worst — a property on a final class with a named type is what
     * arrives in Swift as something that can be observed without casting.
     */
    val state: StateFlow<MatchdayUiState> = viewModel.uiStateFlow

    /**
     * How long until kick-off.
     *
     * The tick belongs to the platform — a SwiftUI timeline and a Compose ticker are different
     * animals — but what a remaining second *means* does not, so the arithmetic stays here. It is
     * also the only way the type reaches Swift: the export carries what the exported API mentions.
     */
    fun countdown(nowMillis: Long, kickoffMillis: Long): Countdown =
        Countdown.between(nowMillis = nowMillis, kickoffMillis = kickoffMillis)

}
