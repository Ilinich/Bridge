package com.begoml.bridge.navigation.swipe

import androidx.compose.runtime.staticCompositionLocalOf

/** Gate for wiring the swipe-to-dismiss gesture. When false the scene renders the foreground
 * directly (no gesture / GraphicsLayer), leaving only the system back. */
val LocalSwipeGestureAvailable = staticCompositionLocalOf { true }

/** True while a swipe-to-dismiss gesture is animating; consumers freeze position-aware effects. */
val LocalSwipeToDismissActive = staticCompositionLocalOf { false }
