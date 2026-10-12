package com.begoml.bridge.foundation.logger.impl

import com.begoml.bridge.foundation.logger.LogLevel

/** The desktop sink is the console, with warnings and errors on the stream a shell can redirect. */
internal actual fun writePlatformLog(
    level: LogLevel,
    tag: String,
    message: String,
    error: Throwable?,
) {
    val line = "[${level.name.uppercase()}] $tag: $message"
    when (level) {
        LogLevel.Warning, LogLevel.Error -> System.err.println(line)
        else -> println(line)
    }
    error?.printStackTrace()
}
