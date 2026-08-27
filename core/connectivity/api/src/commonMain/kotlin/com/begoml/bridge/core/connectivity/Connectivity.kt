package com.begoml.bridge.core.connectivity

import com.begoml.bridge.foundation.tessera.FeatureStateDelegate

/**
 * Whether the device currently has a usable network.
 *
 * [Unknown] is a real third state rather than a placeholder: both platforms answer asynchronously,
 * and a screen that treated the gap as offline would flash a warning on every cold start.
 */
enum class NetworkStatus { Unknown, Online, Offline }

data class ConnectivityState(val status: NetworkStatus = NetworkStatus.Unknown) {

    val isOffline: Boolean get() = status == NetworkStatus.Offline
}

/**
 * A state holder with no screen of its own.
 *
 * It is a singleton the whole app shares, so every consumer sees the same answer and the platform
 * is watched once. Screens fold [stateFlow] into their own ui state instead of owning a copy.
 */
interface ConnectivityFeature : FeatureStateDelegate<ConnectivityState>
