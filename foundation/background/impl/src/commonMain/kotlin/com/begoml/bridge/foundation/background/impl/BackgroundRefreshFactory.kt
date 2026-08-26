package com.begoml.bridge.foundation.background.impl

import com.begoml.bridge.foundation.background.BackgroundRefresh
import org.koin.core.module.Module
import org.koin.dsl.module

internal expect fun Module.bindBackgroundRefresh()

/** Identifier the platform schedulers register the daily refresh under. */
internal const val RefreshTaskId = "com.begoml.bridge.refresh"

internal const val RefreshIntervalHours = 24L

fun backgroundModule() = module {
    bindBackgroundRefresh()
}

/** Does nothing, for a platform with no background scheduling worth the wiring. */
internal class NoBackgroundRefresh : BackgroundRefresh {
    override fun schedule() = Unit
}
