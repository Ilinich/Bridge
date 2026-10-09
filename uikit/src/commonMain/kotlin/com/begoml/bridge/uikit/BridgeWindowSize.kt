package com.begoml.bridge.uikit

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * How much room the window has, in the three sizes this app actually does something different in.
 *
 * Not a breakpoint per screen: a screen that decides its own thresholds is a screen that
 * disagrees with the next one at 712 points. The host measures once and everything reads the
 * same answer, which is what keeps a wide window looking like one design rather than four.
 */
enum class BridgeWindowSize {

    /** A phone, or a window the size of one. The design's home. */
    Compact,

    /** A tablet in portrait, a split-screen desktop window. */
    Medium,

    /** A desktop window or a browser tab opened on a real screen. */
    Expanded;

    /**
     * How wide the content is allowed to run.
     *
     * Every screen here is a single column, and a column has a measure beyond which it stops
     * being readable — so the extra width goes into the grid's columns rather than into longer
     * lines of text.
     */
    val contentWidth: Dp
        get() = when (this) {
            Compact -> Dp.Unspecified
            Medium -> 640.dp
            Expanded -> 860.dp
        }

    /** How many cards fit across the squad before they stop looking like cards. */
    val gridColumns: Int
        get() = when (this) {
            Compact -> 2
            Medium -> 3
            Expanded -> 4
        }

    companion object {

        /** The Material window-size breakpoints, which is what every platform here agrees on. */
        fun of(width: Dp): BridgeWindowSize = when {
            width < 600.dp -> Compact
            width < 840.dp -> Medium
            else -> Expanded
        }
    }
}

val LocalWindowSize = compositionLocalOf { BridgeWindowSize.Compact }
