package com.begoml.bridge.foundation.logger.impl

import com.begoml.bridge.foundation.logger.LogLevel

/**
 * The browser console, reached through `println` rather than through `console.error`.
 *
 * Kotlin/Wasm routes standard output to the console already, and a direct `console.error` call
 * would need an external declaration per level for a sink nobody reads programmatically.
 */
internal actual fun writePlatformLog(
    level: LogLevel,
    tag: String,
    message: String,
    error: Throwable?,
) {
    val suffix = error?.let { " — ${it.message}" }.orEmpty()
    println("[${level.name.uppercase()}] $tag: $message$suffix")
}
