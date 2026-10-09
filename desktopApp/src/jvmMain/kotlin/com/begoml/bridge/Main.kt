package com.begoml.bridge

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.begoml.bridge.core.background.BackgroundRefresh
import com.begoml.bridge.di.startBridge

/**
 * The desktop entry point.
 *
 * The window opens at the proportions the screens were designed for rather than at whatever the
 * window manager offers: every layout in this app is a single column, and a column stretched to
 * a 2560-point window is a line of text nobody can follow.
 */
fun main() {
    startBridge().koin.get<BackgroundRefresh>().schedule()

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Bridge",
            state = rememberWindowState(size = DpSize(width = 420.dp, height = 880.dp)),
        ) {
            App()
        }
    }
}
