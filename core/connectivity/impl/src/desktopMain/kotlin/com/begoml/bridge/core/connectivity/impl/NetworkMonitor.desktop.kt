package com.begoml.bridge.core.connectivity.impl

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import org.koin.core.module.Module
import java.net.NetworkInterface
import kotlin.time.Duration.Companion.seconds

internal actual fun Module.bindNetworkMonitor() {
    single<NetworkMonitor> { DesktopNetworkMonitor() }
}

/**
 * Whether any interface other than loopback is up, polled.
 *
 * The JVM has no callback for this — `NetworkInterface` is a snapshot — so the poll is the API,
 * not a workaround for one. Five seconds is chosen against what the value is for: a banner that
 * says the screen is showing what it had, which nobody times with a stopwatch.
 */
private class DesktopNetworkMonitor : NetworkMonitor {

    override fun updates(): Flow<Boolean> = flow {
        while (true) {
            emit(isUp())
            delay(PollInterval)
        }
    }.distinctUntilChanged()

    private fun isUp(): Boolean = runCatching {
        NetworkInterface.getNetworkInterfaces()
            .toList()
            .any { it.isUp && !it.isLoopback }
    }.getOrDefault(true)

    private companion object {
        val PollInterval = 5.seconds
    }
}
