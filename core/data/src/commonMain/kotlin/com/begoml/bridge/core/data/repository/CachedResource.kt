package com.begoml.bridge.core.data.repository

import com.begoml.bridge.core.data.model.Loadable
import com.begoml.bridge.foundation.cache.InMemoryCache
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * Turns a cache entry into the three states a screen can be in.
 *
 * Collecting triggers a load that honours the cache's time to live, so a screen never has to ask
 * for data separately from observing it. A failure is reported only while nothing is cached; once
 * a value exists, a failed refresh leaves the screen showing what it has.
 */
internal fun <Key : Any, Value : Any> InMemoryCache<Key, Value>.cachedResource(
    key: Key,
): Flow<Loadable<Value>> = flow {
    val cached = peek(key)
    emit(if (cached == null) Loadable.Loading else Loadable.Content(cached))

    val failure = runCatching { get(key) }.exceptionOrNull()
    if (failure != null && peek(key) == null) emit(Loadable.Failed(failure))

    emitAll(observe(key).filterNotNull().map { value -> Loadable.Content(value) })
}
