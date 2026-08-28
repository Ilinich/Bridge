package com.begoml.bridge

import androidx.compose.ui.window.ComposeUIViewController
import com.begoml.bridge.di.startBridge
import com.begoml.bridge.core.background.BackgroundRefresh

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
        // Registration has to happen before the app finishes launching, which on iOS is the first
        // time this controller is built; BGTaskScheduler throws if it is asked any later.
        startBridge().koin.get<BackgroundRefresh>().schedule()
    }
    App()
}
