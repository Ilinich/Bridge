package com.begoml.bridge.foundation.cache

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.time.Duration

/**
 * An in-memory cache with a soft and a hard time to live.
 *
 * Age is measured from the last completed load, and produces three zones:
 * - **fresh** — younger than [staleAfter]: the cached value is returned, nothing is fetched;
 * - **stale** — at least [staleAfter] but younger than [expireAfter]: the cached value is returned
 *   immediately *and* a refresh runs on [backgroundScope];
 * - **expired** — at least [expireAfter]: the caller waits for a fresh value.
 *
 * Both thresholds are optional. With neither set the cache is a plain memoiser with no notion of
 * time. [staleAfter] needs a [backgroundScope] to revalidate on, which is checked at construction
 * rather than discovered at the first stale read.
 *
 * **Concurrency.** A single [Mutex] serialises every read-modify-write of the cache maps, and is
 * the only serialisation point. Nothing is ever awaited while it is held. Two consequences are
 * deliberate:
 * - concurrent loads of one key share a single [CompletableDeferred], so N callers cause one call
 *   to [loader] and all of them see the same outcome;
 * - a load carries the key's generation, taken before the call and compared under the lock before
 *   the write. [invalidate] bumps that generation, so a response that was already in flight when
 *   the key was invalidated is delivered to its caller and **not** written to the cache. Without
 *   it, a slow response could resurrect data the caller had just thrown away.
 *
 * [loader] runs on [dispatcher]; the cache never assumes a dispatcher of its own, so tests stay
 * deterministic. [nowMillis] is injected for the same reason.
 */
class InMemoryCache<Key : Any, Value : Any>(
    private val loader: suspend (Key) -> Value,
    private val dispatcher: CoroutineDispatcher,
    private val nowMillis: () -> Long,
    private val staleAfter: Duration? = null,
    private val expireAfter: Duration? = null,
    private val backgroundScope: CoroutineScope? = null,
) {

    init {
        require(staleAfter == null || backgroundScope != null) {
            "staleAfter needs a backgroundScope to revalidate on"
        }
        require(staleAfter == null || expireAfter == null || staleAfter <= expireAfter) {
            "staleAfter ($staleAfter) must not exceed expireAfter ($expireAfter)"
        }
    }

    enum class Strategy {
        /** Return what is cached; fail if nothing is. Never touches [loader]. */
        CACHE_ONLY,

        /** Honour the age zones above. The default. */
        LOAD_IF_NO_CACHE,

        /** Always load, and wait for the result. */
        REFRESH_AND_GET,
    }

    private class Entry<Value>(val value: Value, val loadedAtMillis: Long)

    private val mutex = Mutex()
    private val entries = MutableStateFlow<Map<Key, Entry<Value>>>(emptyMap())
    private val generations = mutableMapOf<Key, Long>()
    private val inFlight = mutableMapOf<Key, CompletableDeferred<Value>>()

    /** Emits the cached value for [key], and null while there is none. */
    fun observe(key: Key): Flow<Value?> =
        entries.map { snapshot -> snapshot[key]?.value }.distinctUntilChanged()

    /** The cached value for [key] without loading anything. */
    fun peek(key: Key): Value? = entries.value[key]?.value

    suspend fun get(key: Key, strategy: Strategy = Strategy.LOAD_IF_NO_CACHE): Value =
        when (strategy) {
            Strategy.REFRESH_AND_GET -> loadOnce(key)
            Strategy.CACHE_ONLY -> checkNotNull(entries.value[key]) {
                "No cached value for $key"
            }.value

            Strategy.LOAD_IF_NO_CACHE -> getWithinTimeToLive(key)
        }

    private suspend fun getWithinTimeToLive(key: Key): Value {
        val entry = entries.value[key] ?: return loadOnce(key)

        val age = nowMillis() - entry.loadedAtMillis
        if (expireAfter != null && age >= expireAfter.inWholeMilliseconds) return loadOnce(key)
        if (staleAfter != null && age >= staleAfter.inWholeMilliseconds) {
            backgroundScope?.launch { runCatching { loadOnce(key) } }
        }
        return entry.value
    }

    suspend fun refresh(key: Key): Value = loadOnce(key)

    /**
     * Drops the cached value for [key] and disowns any load already in flight for it.
     *
     * The in-flight caller still receives its result; the result is simply not written back.
     */
    suspend fun invalidate(key: Key) {
        mutex.withLock {
            entries.value = entries.value - key
            generations[key] = (generations[key] ?: 0L) + 1L
        }
    }

    suspend fun invalidateAll() {
        mutex.withLock {
            entries.value.keys.forEach { key ->
                generations[key] = (generations[key] ?: 0L) + 1L
            }
            entries.value = emptyMap()
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun loadOnce(key: Key): Value {
        val owned = CompletableDeferred<Value>()
        var generationAtStart = 0L

        val alreadyRunning = mutex.withLock {
            val existing = inFlight[key]
            if (existing == null) {
                inFlight[key] = owned
                generationAtStart = generations[key] ?: 0L
            }
            existing
        }
        if (alreadyRunning != null) return alreadyRunning.await()

        return try {
            val value = withContext(dispatcher) { loader(key) }
            // NonCancellable because the caller may already be cancelled by the time the loader
            // returns: taking the mutex would then throw before the key is released, and every
            // later caller would await a deferred nobody will ever complete.
            withContext(NonCancellable) {
                mutex.withLock {
                    if ((generations[key] ?: 0L) == generationAtStart) {
                        entries.value = entries.value + (key to Entry(value, nowMillis()))
                    }
                    inFlight.remove(key)
                }
            }
            owned.complete(value)
            value
        } catch (error: Throwable) {
            withContext(NonCancellable) { mutex.withLock { inFlight.remove(key) } }
            owned.completeExceptionally(error)
            throw error
        }
    }
}
