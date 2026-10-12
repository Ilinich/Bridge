package com.begoml.bridge.uikit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.awt.Desktop
import java.net.URI

/**
 * `Desktop.browse` is unsupported on a headless JVM and on some Linux desktops, where it throws
 * rather than returning false — a link that cannot be opened must not take the window with it.
 */
@Composable
actual fun rememberUrlOpener(): UrlOpener = remember {
    UrlOpener { url ->
        runCatching {
            val desktop = Desktop.getDesktop()
            if (desktop.isSupported(Desktop.Action.BROWSE)) desktop.browse(URI(url.asWebUri()))
        }
    }
}
