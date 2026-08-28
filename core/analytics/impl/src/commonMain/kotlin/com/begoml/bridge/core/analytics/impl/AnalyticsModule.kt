package com.begoml.bridge.core.analytics.impl

import com.begoml.bridge.core.analytics.Analytics
import org.koin.dsl.module

fun analyticsModule() = module {
    single<Analytics> { LoggingAnalytics(logger = get()) }
}
