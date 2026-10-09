package com.begoml.bridge.navigation.swipe

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp

/** A canvas in a page has no device corners to round to. */
@Composable
actual fun deviceCornerRadius(): Dp? = null
