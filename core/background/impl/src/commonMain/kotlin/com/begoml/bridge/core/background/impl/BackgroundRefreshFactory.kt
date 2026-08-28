package com.begoml.bridge.core.background.impl

import org.koin.core.module.Module
import org.koin.dsl.module

internal expect fun Module.bindBackgroundRefresh()

/** Identifier the platform schedulers register the daily refresh under. */
internal const val RefreshTaskId = "com.begoml.bridge.refresh"

internal const val RefreshIntervalHours = 24L

fun backgroundModule() = module {
    bindBackgroundRefresh()
}

