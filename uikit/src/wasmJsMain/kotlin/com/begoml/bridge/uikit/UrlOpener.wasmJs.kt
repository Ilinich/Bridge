package com.begoml.bridge.uikit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.browser.window

/** A new tab, with `noopener` so the page being opened cannot reach back through `window.opener`. */
@Composable
actual fun rememberUrlOpener(): UrlOpener = remember {
    UrlOpener { url -> window.open(url.asWebUri(), target = "_blank", features = "noopener") }
}
