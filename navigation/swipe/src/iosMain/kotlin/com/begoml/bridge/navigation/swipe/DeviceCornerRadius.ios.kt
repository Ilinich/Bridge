package com.begoml.bridge.navigation.swipe

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp

/**
 * UIKit exposes the display's corner radius only privately, and reading it is grounds for
 * rejection, so the fraction-of-width fallback stands here.
 */
@Composable
actual fun deviceCornerRadius(): Dp? = null
