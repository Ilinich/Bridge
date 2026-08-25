package com.begoml.bridge.navigation.swipe

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.unit.IntSize
import androidx.navigationevent.NavigationEventTransitionState
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

private const val SETTLE_TAIL_MS = 350L

/**
 * Observes whether a system predictive-back gesture is currently in progress.
 *
 * Reads the passive [androidx.navigationevent.NavigationEventDispatcher.transitionState] StateFlow,
 * so NavDisplay's own predictive-pop handling is unaffected. The dispatcher reports Idle the moment
 * the finger is lifted — before the commit/cancel settle animation finishes — so the flag is held
 * for an extra [SETTLE_TAIL_MS] after Idle (cancelled if a new gesture starts).
 */
@Composable
fun rememberPredictiveBackInProgress(): State<Boolean> {
    val dispatcher = LocalNavigationEventDispatcherOwner.current?.navigationEventDispatcher
    return if (dispatcher == null) {
        remember { mutableStateOf(false) }
    } else {
        produceState(initialValue = false, dispatcher) {
            dispatcher.transitionState.collectLatest { state ->
                if (state is NavigationEventTransitionState.InProgress) {
                    value = true
                } else {
                    delay(SETTLE_TAIL_MS)
                    value = false
                }
            }
        }
    }
}

/**
 * Freezes the draw output of downstream modifiers while the screen is being dragged off-screen —
 * either by the in-app swipe-to-dismiss gesture or by the system predictive-back gesture.
 *
 * While idle, content is recorded into a GraphicsLayer every frame and drawn normally. Once a
 * gesture starts, the last recorded snapshot is replayed instead of `drawContent()`, which prevents
 * position-aware effects (e.g. Haze blur) from recalculating against the moving coordinate space.
 * Apply BEFORE `.hazeEffect()` in the modifier chain.
 *
 * A gesture that is already in progress on the very first draw leaves nothing to replay. Drawing
 * live is the only honest answer there: skipping the draw would publish an empty frame, and whatever
 * sits behind this modifier would be what the user sees.
 */
@Composable
fun Modifier.freezeDuringSwipeToDismiss(): Modifier {
    val isSwiping = LocalSwipeToDismissActive.current
    val isPredictiveBack by rememberPredictiveBackInProgress()
    val isFrozen = isSwiping || isPredictiveBack
    val layer = rememberGraphicsLayer()
    val hasSnapshot = remember { mutableStateOf(false) }
    return this.then(
        Modifier.drawWithContent {
            if (!isFrozen) {
                layer.record(
                    size = IntSize(size.width.toInt(), size.height.toInt()),
                ) {
                    this@drawWithContent.drawContent()
                }
                drawLayer(layer)
                hasSnapshot.value = true
            } else if (hasSnapshot.value) {
                drawLayer(layer)
            } else {
                drawContent()
            }
        }
    )
}
