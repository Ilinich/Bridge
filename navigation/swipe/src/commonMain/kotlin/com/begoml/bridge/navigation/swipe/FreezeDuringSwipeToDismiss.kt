package com.begoml.bridge.navigation.swipe

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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

private const val SettleTailMillis = 350L

/**
 * Replays the last drawn frame of everything downstream while a screen is being dragged away.
 *
 * Use it for content whose position moves relative to what it samples — a blur inside the screen
 * being dragged, for instance. It does **not** stop a blur from following live content: a recorded
 * layer replays draw commands, and a command that samples a source layer samples the source it
 * finds at replay time. Measured on the app's own tab bar: its colour tracked the screen sliding
 * underneath it with the freeze applied.
 *
 * Must sit **before** the effect it freezes in the modifier chain, so it intercepts the draw call
 * rather than being intercepted by it.
 */
@Composable
fun Modifier.freezeDuringSwipeToDismiss(): Modifier {
    val signal = LocalSwipeDismissSignal.current
    val predictiveBack by rememberPredictiveBackInProgress()
    val layer = rememberGraphicsLayer()
    var hasSnapshot by remember { mutableStateOf(false) }
    return this.then(
        Modifier.drawWithContent {
            if (signal.isActive || predictiveBack) {
                if (hasSnapshot) drawLayer(layer)
                return@drawWithContent
            }
            layer.record(size = IntSize(size.width.toInt(), size.height.toInt())) {
                this@drawWithContent.drawContent()
            }
            drawLayer(layer)
            if (!hasSnapshot) hasSnapshot = true
        },
    )
}

/**
 * Observes whether a system predictive-back gesture is currently in progress.
 *
 * Reads the passive [androidx.navigationevent.NavigationEventDispatcher.transitionState] StateFlow,
 * so NavDisplay's own predictive-pop handling is unaffected. The dispatcher reports Idle the moment
 * the finger is lifted — before the commit/cancel settle animation finishes — so the flag is held
 * for an extra [SettleTailMillis] after Idle (cancelled if a new gesture starts).
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
                    delay(SettleTailMillis)
                    value = false
                }
            }
        }
    }
}
