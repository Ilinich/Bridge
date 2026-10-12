package com.begoml.bridge

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.begoml.bridge.di.startBridgeWeb
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * The web entry point.
 *
 * Nothing is composed until the graph is up, because starting it is suspending here: IndexedDB
 * opens asynchronously and the app is better off waiting a frame for it than rendering a screen
 * that has to be told later it had data all along.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    MainScope().launch {
        startBridgeWeb()
        ComposeViewport { App() }
    }
}
