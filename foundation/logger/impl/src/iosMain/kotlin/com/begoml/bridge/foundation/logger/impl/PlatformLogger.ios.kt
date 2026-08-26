package com.begoml.bridge.foundation.logger.impl

import com.begoml.bridge.foundation.logger.LogLevel
import platform.Foundation.NSLog

/**
 * NSLog with a literal format string.
 *
 * The message is passed as an argument rather than interpolated into the format: a percent sign
 * arriving in data would otherwise be read as a format specifier and crash the process.
 */
internal actual fun writePlatformLog(
    level: LogLevel,
    tag: String,
    message: String,
    error: Throwable?,
) {
    val suffix = error?.let { " — ${it.message}" }.orEmpty()
    NSLog("%s", "[${level.name.uppercase()}] $tag: $message$suffix")
}
