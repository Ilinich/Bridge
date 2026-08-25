package com.begoml.bridge.uikit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

@Composable
actual fun rememberUrlOpener(): UrlOpener = remember {
    UrlOpener { url ->
        NSURL.URLWithString(url.asWebUri())?.let { UIApplication.sharedApplication.openURL(it) }
    }
}
