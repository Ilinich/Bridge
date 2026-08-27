package com.begoml.bridge.core.connectivity.impl

import com.begoml.bridge.core.connectivity.Connectivity
import com.begoml.bridge.foundation.coroutines.stateHolderScope
import org.koin.core.module.Module
import org.koin.dsl.module

internal expect fun Module.bindNetworkMonitor()

fun connectivityModule(): Module = module {
    bindNetworkMonitor()
    single<Connectivity> { ConnectivityImpl(scope = stateHolderScope(), monitor = get()) }
}
