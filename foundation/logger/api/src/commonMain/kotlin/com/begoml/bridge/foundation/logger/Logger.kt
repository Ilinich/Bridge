package com.begoml.bridge.foundation.logger

/**
 * Where a line goes is not the caller's business.
 *
 * The whole contract is this interface, so a feature depends on the idea of logging and never on
 * a platform log API. The implementation lives in a separate module and is reached only through
 * dependency injection.
 */
interface Logger {

    fun log(level: LogLevel, tag: String, message: String, error: Throwable? = null)
}

enum class LogLevel { Debug, Info, Warning, Error }

fun Logger.debug(tag: String, message: String) = log(LogLevel.Debug, tag, message)

fun Logger.info(tag: String, message: String) = log(LogLevel.Info, tag, message)

fun Logger.warn(tag: String, message: String, error: Throwable? = null) =
    log(LogLevel.Warning, tag, message, error)

fun Logger.error(tag: String, message: String, error: Throwable? = null) =
    log(LogLevel.Error, tag, message, error)
