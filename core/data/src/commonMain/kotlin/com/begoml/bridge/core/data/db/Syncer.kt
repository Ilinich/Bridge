package com.begoml.bridge.core.data.db

import kotlin.time.Clock

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.time.Duration

/**
 * Decides whether a resource still needs fetching, and makes sure it is fetched only once.
 *
 * The decision is taken against a timestamp **on disk**, so a cold start after a process death
 * knows what it already has. A null [ttl] means the resource can never change — a finished season
 * is fetched exactly once in the lifetime of an install.
 *
 * The per-key mutex is what keeps two screens asking for the same thing from producing two
 * requests; it is held across the fetch on purpose.
 */
internal class Syncer(
    private val freshness: FreshnessDao,
    private val clock: Clock,
    private val dispatcher: CoroutineDispatcher,
) {

    private val locksGuard = Mutex()
    private val locks = mutableMapOf<String, Mutex>()

    suspend fun sync(key: String, ttl: Duration?, force: Boolean = false, fetch: suspend () -> Unit) {
        lockFor(key).withLock {
            if (!force && !isDue(key, ttl)) return
            withContext(dispatcher) { fetch() }
            freshness.stamp(FreshnessEntity(key = key, fetchedAtMillis = clock.now().toEpochMilliseconds()))
        }
    }

    private suspend fun isDue(key: String, ttl: Duration?): Boolean {
        val fetchedAt = freshness.fetchedAt(key) ?: return true
        if (ttl == null) return false
        return clock.now().toEpochMilliseconds() - fetchedAt >= ttl.inWholeMilliseconds
    }

    private suspend fun lockFor(key: String): Mutex =
        locksGuard.withLock { locks.getOrPut(key) { Mutex() } }
}
