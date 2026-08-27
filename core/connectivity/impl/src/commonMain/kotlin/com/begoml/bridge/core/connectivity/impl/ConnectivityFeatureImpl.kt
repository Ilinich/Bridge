package com.begoml.bridge.core.connectivity.impl

import com.begoml.bridge.core.connectivity.ConnectivityFeature
import com.begoml.bridge.core.connectivity.ConnectivityState
import com.begoml.bridge.core.connectivity.NetworkStatus
import com.begoml.bridge.foundation.tessera.SimpleFeature
import com.begoml.bridge.foundation.tessera.feature
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/** Reports what the platform says about the network, and nothing about what to do with it. */
internal fun interface NetworkMonitor {

    fun updates(): Flow<Boolean>
}

internal sealed interface ConnectivityAction

internal sealed interface ConnectivityEvent

internal class ConnectivityFeatureImpl(
    scope: CoroutineScope,
    monitor: NetworkMonitor,
) : ConnectivityFeature,
    SimpleFeature<ConnectivityState, ConnectivityAction, ConnectivityEvent>
    by feature(ConnectivityState(), scope) {

    init {
        scope.launch {
            monitor.updates().distinctUntilChanged().collect { isOnline ->
                val status = if (isOnline) NetworkStatus.Online else NetworkStatus.Offline
                updateState { ConnectivityState(status) }
            }
        }
    }
}
