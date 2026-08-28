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

    /**
     * The current answer, starting at [NetworkStatus.Unknown] until the platform has given one.
     *
     * Hot and shared: the platform is watched once for the whole app, and collecting this neither
     * starts nor stops that. On Android it reports a *validated* network, so a connection that
     * leads nowhere — a captive portal, a router with no route upstream — reads as offline, which
     * is what a caller means by the question.
     */
    val status: StateFlow<NetworkStatus>

    /** A snapshot for a caller that is not collecting; [NetworkStatus.Unknown] is not offline. */
    val isOffline: Boolean get() = status.value == NetworkStatus.Offline
}
