package com.begoml.bridge.navigation.swipe

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp

/**
 * The screen's own corner radius, when the platform will say.
 *
 * A screen dragged aside should round to the radius of the hole it is sliding out of; anything
 * else reads as a card that does not belong to this device. Null where the platform keeps the
 * number to itself, and the caller falls back to a fraction of the screen width.
 */
@Composable
expect fun deviceCornerRadius(): Dp?
