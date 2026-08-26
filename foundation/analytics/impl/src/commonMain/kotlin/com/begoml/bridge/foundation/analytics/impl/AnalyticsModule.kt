package com.begoml.bridge.foundation.analytics.impl

import com.begoml.bridge.foundation.analytics.Analytics
import org.koin.dsl.module

fun analyticsModule() = module {
    single<Analytics> { LoggingAnalytics(logger = get()) }
}
