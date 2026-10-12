package com.begoml.bridge.core.background.impl

import com.begoml.bridge.core.background.BackgroundRefresh
import org.koin.core.module.Module

internal actual fun Module.bindBackgroundRefresh() {
    single<BackgroundRefresh> { NoBackgroundRefresh }
}

/**
 * A desktop app refreshes when it is open, and when it is closed it is not running.
 *
 * There is no WorkManager and no BGTaskScheduler here: a daemon that wakes to refresh a football
 * calendar would be a background process a person did not ask to install. The contract already
 * says nothing may depend on this having happened.
 */
private object NoBackgroundRefresh : BackgroundRefresh {

    override fun schedule() = Unit
}
