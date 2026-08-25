package com.begoml.bridge

import androidx.compose.ui.window.ComposeUIViewController
import com.begoml.bridge.di.startBridge

private var started = false

/**
 * The iOS entry point.
 *
 * iOS has no Application class to start the graph from, so it is started here and guarded — the
 * view controller can be created more than once, and Koin refuses a second start.
 */
@Suppress("FunctionNaming")
fun MainViewController() = ComposeUIViewController {
    if (!started) {
        started = true
        startBridge()
    }
    App()
}
