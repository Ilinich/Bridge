package com.begoml.bridge

import androidx.compose.ui.window.ComposeUIViewController

/**
 * The Compose screens, for as long as both UIs live side by side.
 *
 * Starting the graph is no longer this function's job — see [IosBridge].
 */
@Suppress("FunctionNaming")
fun MainViewController() = ComposeUIViewController {
    IosBridge.start()
    App()
}
