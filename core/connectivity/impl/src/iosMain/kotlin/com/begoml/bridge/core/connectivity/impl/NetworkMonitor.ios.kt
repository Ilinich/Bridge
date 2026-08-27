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
import platform.darwin.dispatch_get_main_queue

internal actual fun Module.bindNetworkMonitor() {
    single<NetworkMonitor> { IosNetworkMonitor() }
}

/**
 * Watches every interface through NWPathMonitor.
 *
 * The handler is delivered on the main queue so the emission is already on the dispatcher the
 * state holder updates from; the monitor is cancelled when the flow's collector goes away.
 */
private class IosNetworkMonitor : NetworkMonitor {

    @OptIn(ExperimentalForeignApi::class)
    override fun updates(): Flow<Boolean> = callbackFlow {
        val monitor = nw_path_monitor_create()
        nw_path_monitor_set_queue(monitor, dispatch_get_main_queue())
        nw_path_monitor_set_update_handler(monitor) { path ->
            trySend(nw_path_get_status(path) == nw_path_status_satisfied)
        }
        nw_path_monitor_start(monitor)
        awaitClose { nw_path_monitor_cancel(monitor) }
    }
}
