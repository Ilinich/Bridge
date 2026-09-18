package com.begoml.bridge.feature.matches.matchday

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow

/**
 * What a platform UI holds instead of a ViewModel store.
 *
 * Android has somewhere to put a state holder and something that empties it; SwiftUI has neither,
 * so the thing it holds has to say when it is done. The scope is the same one the state holder
 * runs on, which is why closing this ends its work rather than merely dropping a reference.
 */
class MatchdayComponent internal constructor(
    private val scope: CoroutineScope,
    val viewModel: MatchdayViewModel,
) {

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

    fun close() {
        scope.cancel()
    }
}
