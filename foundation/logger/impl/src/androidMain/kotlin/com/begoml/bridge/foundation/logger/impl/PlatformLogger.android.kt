package com.begoml.bridge.foundation.logger.impl

import android.util.Log
import com.begoml.bridge.foundation.logger.LogLevel

internal actual fun writePlatformLog(
    level: LogLevel,
    tag: String,
    message: String,
    error: Throwable?,
) {
    when (level) {
        LogLevel.Debug -> Log.d(tag, message, error)
        LogLevel.Info -> Log.i(tag, message, error)
        LogLevel.Warning -> Log.w(tag, message, error)
        LogLevel.Error -> Log.e(tag, message, error)
    }
}
