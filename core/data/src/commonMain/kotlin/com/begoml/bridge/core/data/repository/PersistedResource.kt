package com.begoml.bridge.core.data.repository

import com.begoml.bridge.core.domain.model.Loadable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * A resource read from disk and refreshed from the network.
 *
 * Stored values are emitted as soon as the database can produce them, without waiting for the
 * network — that is the whole point of persisting them. A failed refresh is reported only when
 * there is nothing stored to show instead.
 */
internal fun <T : Any> persistedResource(
    stored: Flow<T?>,
    sync: suspend () -> Unit,
): Flow<Loadable<T>> = channelFlow {
    send(Loadable.Loading)

    val mirror = launch {
        stored.collect { value -> if (value != null) send(Loadable.Content(value)) }
    }

    val failure = runCatching { sync() }.exceptionOrNull()
    if (failure != null && stored.first() == null) send(Loadable.Failed(failure))

    awaitClose { mirror.cancel() }
}
