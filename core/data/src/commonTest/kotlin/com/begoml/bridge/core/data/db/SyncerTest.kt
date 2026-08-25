package com.begoml.bridge.core.data.db

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.hours

private class FakeFreshnessDao : FreshnessDao {

    private val stamps = mutableMapOf<String, Long>()

    override suspend fun fetchedAt(key: String): Long? = stamps[key]

    override suspend fun stamp(entry: FreshnessEntity) {
        stamps[entry.key] = entry.fetchedAtMillis
    }
}

private const val Key = "season:2025-26"

class SyncerTest {

    private fun TestScope.syncerWith(now: () -> Long, dao: FreshnessDao) =
        Syncer(freshness = dao, nowMillis = now, dispatcher = StandardTestDispatcher(testScheduler))

    @Test
    fun `a resource that was never fetched is fetched`() = runTest {
        var fetches = 0
        syncerWith({ 0L }, FakeFreshnessDao()).sync(Key, ttl = 4.hours) { fetches++ }

        assertEquals(1, fetches)
    }

    @Test
    fun `a resource that can never change is fetched once and never again`() = runTest {
        var fetches = 0
        val syncer = syncerWith({ 0L }, FakeFreshnessDao())

        syncer.sync(Key, ttl = null) { fetches++ }
        syncer.sync(Key, ttl = null) { fetches++ }

        assertEquals(1, fetches, "a finished season must cost exactly one request")
    }

    @Test
    fun `a fresh resource is not fetched again`() = runTest {
        var fetches = 0
        var now = 0L
        val syncer = syncerWith({ now }, FakeFreshnessDao())

        syncer.sync(Key, ttl = 4.hours) { fetches++ }
        now = 3.hours.inWholeMilliseconds
        syncer.sync(Key, ttl = 4.hours) { fetches++ }

        assertEquals(1, fetches)
    }

    @Test
    fun `a resource past its time to live is fetched again`() = runTest {
        var fetches = 0
        var now = 0L
        val syncer = syncerWith({ now }, FakeFreshnessDao())

        syncer.sync(Key, ttl = 4.hours) { fetches++ }
        now = 5.hours.inWholeMilliseconds
        syncer.sync(Key, ttl = 4.hours) { fetches++ }

        assertEquals(2, fetches)
    }

    @Test
    fun `force ignores a stamp that is still fresh`() = runTest {
        var fetches = 0
        val syncer = syncerWith({ 0L }, FakeFreshnessDao())

        syncer.sync(Key, ttl = 4.hours) { fetches++ }
        syncer.sync(Key, ttl = 4.hours, force = true) { fetches++ }

        assertEquals(2, fetches)
    }

    @Test
    fun `two callers asking at once produce one fetch`() = runTest {
        val gate = CompletableDeferred<Unit>()
        var fetches = 0
        val syncer = syncerWith({ 0L }, FakeFreshnessDao())

        val first = async { syncer.sync(Key, ttl = 4.hours) { fetches++; gate.await() } }
        val second = async { syncer.sync(Key, ttl = 4.hours) { fetches++ } }
        testScheduler.advanceUntilIdle()
        gate.complete(Unit)
        first.await()
        second.await()

        assertEquals(1, fetches, "the second caller must see the first one's result")
    }
}
