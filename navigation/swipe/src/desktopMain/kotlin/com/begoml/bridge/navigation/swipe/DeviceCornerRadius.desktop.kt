package com.begoml.bridge.navigation.swipe

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp

/** A window has the corners its window manager draws, and does not report them. */
@Composable
actual fun deviceCornerRadius(): Dp? = null
