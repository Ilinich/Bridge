package com.begoml.bridge.core.connectivity

import kotlinx.coroutines.flow.StateFlow

/**
 * Whether the device currently has a usable network.
 *
 * [Unknown] is a real third state rather than a placeholder: both platforms answer asynchronously,
 * and a screen that treated the gap as offline would flash a warning on every cold start.
 */
enum class NetworkStatus { Unknown, Online, Offline }

/**
 * What the platform says about the network, and nothing about what to do with it.
 *
 * Deliberately not a state holder: there is no state of ours to keep here and nothing to dispatch
 * into it. It reports an outside fact, so consumers fold it into their own state.
 */
interface Connectivity {

    val status: StateFlow<NetworkStatus>

    val isOffline: Boolean get() = status.value == NetworkStatus.Offline
}
