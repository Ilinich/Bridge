package com.begoml.bridge.foundation.logger.impl

import com.begoml.bridge.foundation.logger.Logger
import org.koin.dsl.module

fun loggerModule(isLoggingEnabled: Boolean) = module {
    single<Logger> { ConsoleLogger(isEnabled = isLoggingEnabled) }
}
