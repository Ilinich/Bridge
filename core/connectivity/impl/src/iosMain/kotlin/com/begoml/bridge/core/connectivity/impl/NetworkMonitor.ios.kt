package com.begoml.bridge.core.connectivity.impl

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.koin.core.module.Module
import platform.Network.nw_path_get_status
import platform.Network.nw_path_monitor_cancel
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_status_satisfied
import platform.darwin.dispatch_queue_create

internal actual fun Module.bindNetworkMonitor() {
    single<NetworkMonitor> { IosNetworkMonitor() }
}

/**
 * Watches every interface through NWPathMonitor.
 *
 * The handler runs on a queue of its own rather than on the main one: nothing here touches UIKit,
 * `trySend` is safe from any thread, and the collector decides where the value is handled. Putting
 * it on the main queue would spend the thread that draws on an event no one is drawing.
 */
private class IosNetworkMonitor : NetworkMonitor {

    @OptIn(ExperimentalForeignApi::class)
    override fun updates(): Flow<Boolean> = callbackFlow {
        val monitor = nw_path_monitor_create()
        val queue = dispatch_queue_create("com.begoml.bridge.connectivity", null)
        nw_path_monitor_set_queue(monitor, queue)
        nw_path_monitor_set_update_handler(monitor) { path ->
            trySend(nw_path_get_status(path) == nw_path_status_satisfied)
        }
        nw_path_monitor_start(monitor)
        awaitClose { nw_path_monitor_cancel(monitor) }
    }
}
