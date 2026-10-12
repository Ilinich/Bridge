package com.begoml.bridge.core.connectivity.impl

import kotlinx.browser.window
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.koin.core.module.Module
import org.w3c.dom.events.Event

internal actual fun Module.bindNetworkMonitor() {
    single<NetworkMonitor> { BrowserNetworkMonitor() }
}

/**
 * `navigator.onLine` plus the two events that change it.
 *
 * The browser reports whether there is a network interface, not whether anything answers on it:
 * a captive portal reads as online here. That is the same lie `NWPathMonitor` tells, and the
 * screens already treat connectivity as a hint — a failed request is what actually decides.
 */
private class BrowserNetworkMonitor : NetworkMonitor {

    override fun updates(): Flow<Boolean> = callbackFlow {
        val listener: (Event) -> Unit = { trySend(window.navigator.onLine) }
        window.addEventListener("online", listener)
        window.addEventListener("offline", listener)
        trySend(window.navigator.onLine)
        awaitClose {
            window.removeEventListener("online", listener)
            window.removeEventListener("offline", listener)
        }
    }
}
