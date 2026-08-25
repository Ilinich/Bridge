package com.begoml.bridge.uikit

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.compositionLocalOf

/**
 * The insets a screen must keep clear: window insets plus the floating tab bar.
 *
 * Provided rather than passed as a parameter on purpose. Navigation caches a `NavEntry` per route,
 * so a screen created on the first frame would capture the padding of that frame forever — and on
 * the first frame the window insets are still zero. Reading it from the composition means every
 * screen sees the current value whenever it recomposes.
 */
val LocalScreenPadding = compositionLocalOf { PaddingValues() }
