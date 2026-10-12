package com.begoml.bridge.core.background.impl

import com.begoml.bridge.core.background.BackgroundRefresh
import org.koin.core.module.Module

internal actual fun Module.bindBackgroundRefresh() {
    single<BackgroundRefresh> { NoBackgroundRefresh }
}

/**
 * A page refreshes while it is open, and when the tab is closed it is gone.
 *
 * A Service Worker with Periodic Background Sync could ask for time, but it is behind an install
 * prompt and shipped by one browser. The contract already
 * says nothing may depend on this having happened.
 */
private object NoBackgroundRefresh : BackgroundRefresh {

    override fun schedule() = Unit
}
