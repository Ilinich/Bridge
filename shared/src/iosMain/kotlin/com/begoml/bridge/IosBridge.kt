package com.begoml.bridge

import com.begoml.bridge.core.background.BackgroundRefresh
import com.begoml.bridge.di.startBridge
import com.begoml.bridge.feature.matches.matchday.MatchdayComponent
import org.koin.core.Koin

/**
 * What Swift calls before it draws anything.
 *
 * The graph used to be started from inside the Compose entry point, which worked only because
 * that entry point was the app. A native UI has several entry points and none of them is the app,
 * so starting is its own call — idempotent, because SwiftUI is free to build a view twice.
 */
object IosBridge {

    private var koin: Koin? = null

    fun start() {
        if (koin != null) return
        koin = startBridge().koin.also { graph ->
            // BGTaskScheduler refuses registrations made after the app has finished launching.
            graph.get<BackgroundRefresh>().schedule()
        }
    }

    fun matchday(): MatchdayComponent = requireNotNull(koin) { "start() first" }.get()
}
