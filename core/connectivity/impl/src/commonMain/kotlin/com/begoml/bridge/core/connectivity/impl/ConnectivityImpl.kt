package com.begoml.bridge.core.connectivity.impl

import com.begoml.bridge.core.connectivity.Connectivity
import com.begoml.bridge.core.connectivity.NetworkStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Reports what the platform says about the network. */
internal fun interface NetworkMonitor {

    fun updates(): Flow<Boolean>
}

internal class ConnectivityImpl(
    scope: CoroutineScope,
    monitor: NetworkMonitor,
) : Connectivity {

    override val status: StateFlow<NetworkStatus> = monitor.updates()
        .distinctUntilChanged()
        .map { isOnline -> if (isOnline) NetworkStatus.Online else NetworkStatus.Offline }
        .stateIn(scope, SharingStarted.Eagerly, NetworkStatus.Unknown)
}
