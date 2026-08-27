package com.begoml.bridge.navigation.swipe

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.Composable

/** Gate for wiring the swipe-to-dismiss gesture. When false the scene renders the foreground
 * directly (no gesture / GraphicsLayer), leaving only the system back. */
val LocalSwipeGestureAvailable = staticCompositionLocalOf { true }

/**
 * Whether a swipe-to-dismiss is running, published to the whole app rather than to the screen
 * being swiped.
 *
 * A holder rather than a `Boolean` behind the composition local, and for two reasons. The local is
 * static, so changing its value would invalidate every consumer under the provider — here that is
 * the entire app; a holder keeps the local's value fixed and moves the change into a state read,
 * which invalidates only the draw scopes that read it. And it lets the signal be provided *above*
 * the navigation host while being written from inside it: the bars drawn over the host are not in
 * the swiped screen's subtree, and they are exactly what needs to know.
 */
@Stable
class SwipeDismissSignal {

    var isActive: Boolean by mutableStateOf(false)
        internal set
}

private val NoSwipeInProgress = SwipeDismissSignal()

val LocalSwipeDismissSignal = staticCompositionLocalOf { NoSwipeInProgress }

@Composable
fun rememberSwipeDismissSignal(): SwipeDismissSignal = remember { SwipeDismissSignal() }
