package com.begoml.bridge.foundation.logger.impl

import com.begoml.bridge.foundation.logger.LogLevel
import com.begoml.bridge.foundation.logger.Logger

/**
 * Writes to the platform console, and only in a debuggable build.
 *
 * The gate is here rather than at each call site: a caller should be able to log freely without
 * deciding whether this particular build wants the line.
 */
internal class ConsoleLogger(private val isEnabled: Boolean) : Logger {

    override fun log(level: LogLevel, tag: String, message: String, error: Throwable?) {
        if (!isEnabled) return
        writePlatformLog(level = level, tag = tag, message = message, error = error)
    }
}
