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

private const val SettleTailMillis = 350L

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
