package com.begoml.bridge.core.connectivity.impl

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module

internal actual fun Module.bindNetworkMonitor() {
    single<NetworkMonitor> { AndroidNetworkMonitor(androidContext()) }
}

/**
 * Watches the default network.
 *
 * Validated capability rather than mere connectivity: a captive portal or a router with no route
 * upstream is a connection the system will happily report as present while every request fails.
 */
private class AndroidNetworkMonitor(context: Context) : NetworkMonitor {

    // Lazy because the graph builds this on start-up while nothing has asked about the network
    // yet, and reaching into system services is work the first frame does not owe anyone.
    private val manager by lazy(LazyThreadSafetyMode.NONE) {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    }

    override fun updates(): Flow<Boolean> = callbackFlow {
        val service = manager
        if (service == null) {
            send(true)
            awaitClose { }
            return@callbackFlow
        }

        val callback = object : ConnectivityManager.NetworkCallback() {

            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                trySend(capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))
            }

            override fun onLost(network: Network) {
                trySend(false)
            }
        }

        service.registerDefaultNetworkCallback(callback)
        awaitClose { service.unregisterNetworkCallback(callback) }
    }
}
