package com.begoml.bridge.core.connectivity.impl

import com.begoml.bridge.core.connectivity.ConnectivityFeature
import com.begoml.bridge.foundation.tessera.stateHolderScope
import org.koin.core.module.Module
import org.koin.dsl.module

internal expect fun Module.bindNetworkMonitor()

fun connectivityModule(): Module = module {
    bindNetworkMonitor()
    single<ConnectivityFeature> {
        ConnectivityFeatureImpl(scope = stateHolderScope(), monitor = get())
    }
}
