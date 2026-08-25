package com.begoml.bridge.uikit

import androidx.compose.runtime.Composable

/** Hands a link to whatever the platform uses to open one. */
fun interface UrlOpener {

    fun open(url: String)
}

@Composable
expect fun rememberUrlOpener(): UrlOpener

/** The feed stores links without a scheme, and neither platform will open one without it. */
internal fun String.asWebUri(): String =
    if (startsWith("http://") || startsWith("https://")) this else "https://$this"
