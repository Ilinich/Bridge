package com.begoml.bridge.foundation.logger.impl

import com.begoml.bridge.foundation.logger.LogLevel

/**
 * The one line of platform code the logger needs.
 *
 * Everything else about logging — levels, tags, the call sites — is common; only the sink differs,
 * so only the sink is expect/actual.
 */
internal expect fun writePlatformLog(
    level: LogLevel,
    tag: String,
    message: String,
    error: Throwable?,
)
