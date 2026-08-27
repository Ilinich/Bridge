package com.begoml.bridge.foundation.cache

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

private const val Key = "key"

class InMemoryCacheTest {

    @Test
    fun `fresh entry is served without loading`() = runTest {
        var loads = 0
        val clock = MovableClock()
        val cache = cacheOf(
            clock = clock,
            staleAfter = 100.milliseconds,
            expireAfter = 500.milliseconds,
            scope = this,
        ) { loads++; "v$loads" }

        assertEquals("v1", cache.get(Key))
        clock.millis = 50
        assertEquals("v1", cache.get(Key))

        assertEquals(1, loads)
    }

    @Test
    fun `stale entry is served immediately and revalidated in the background`() = runTest {
        var loads = 0
        val clock = MovableClock()
        val cache = cacheOf(
            clock = clock,
            staleAfter = 100.milliseconds,
            expireAfter = 500.milliseconds,
            scope = this,
        ) { loads++; "v$loads" }

        assertEquals("v1", cache.get(Key))
        clock.millis = 200

        assertEquals("v1", cache.get(Key), "stale reads must not block on the network")
        testScheduler.advanceUntilIdle()

        assertEquals(2, loads, "a stale read must trigger exactly one background refresh")
        assertEquals("v2", cache.peek(Key))
    }

    @Test
    fun `expired entry makes the caller wait for a fresh value`() = runTest {
        var loads = 0
        val clock = MovableClock()
        val cache = cacheOf(
            clock = clock,
            staleAfter = 100.milliseconds,
            expireAfter = 500.milliseconds,
            scope = this,
        ) { loads++; "v$loads" }

        assertEquals("v1", cache.get(Key))
        clock.millis = 600

        assertEquals("v2", cache.get(Key))
        assertEquals(2, loads)
    }

    @Test
    fun `concurrent loads of one key call the loader once`() = runTest {
        val gate = CompletableDeferred<Unit>()
        var loads = 0
        val cache = cacheOf(clock = MovableClock(), scope = this) {
            loads++
            gate.await()
            "value"
        }

        val first = async { cache.get(Key) }
        val second = async { cache.get(Key) }
        val third = async { cache.refresh(Key) }
        testScheduler.advanceUntilIdle()

        gate.complete(Unit)

        assertEquals("value", first.await())
        assertEquals("value", second.await())
        assertEquals("value", third.await())
        assertEquals(1, loads, "three concurrent callers must share one load")
    }

    @Test
    fun `a response that lands after invalidate is not written back`() = runTest {
        val gate = CompletableDeferred<Unit>()
        val cache = cacheOf(clock = MovableClock(), scope = this) {
            gate.await()
            "late"
        }

        val pending = async { cache.get(Key) }
        testScheduler.advanceUntilIdle()

        cache.invalidate(Key)
        gate.complete(Unit)

        assertEquals("late", pending.await(), "the caller still receives its own result")
        assertNull(cache.peek(Key), "an invalidated key must not be resurrected by a late response")
    }

    @Test
    fun `a failing background refresh leaves the cached value in place`() = runTest {
        var attempt = 0
        val clock = MovableClock()
        val cache = cacheOf(
            clock = clock,
            staleAfter = 100.milliseconds,
            scope = this,
        ) {
            attempt++
            if (attempt == 1) "v1" else error("network down")
        }

        assertEquals("v1", cache.get(Key))
        clock.millis = 200

        assertEquals("v1", cache.get(Key))
        testScheduler.advanceUntilIdle()

        assertEquals("v1", cache.observe(Key).first())
        assertEquals(2, attempt, "the refresh must have been attempted")
    }

    @Test
    fun `a failing load propagates to every waiting caller`() = runTest {
        val cache = cacheOf(clock = MovableClock(), scope = this) { error("network down") }

        assertFailsWith<IllegalStateException> { cache.get(Key) }
        assertNull(cache.peek(Key))
    }

    @Test
    fun `staleAfter without a background scope is rejected at construction`() {
        val failure = assertFailsWith<IllegalArgumentException> {
            InMemoryCache<String, String>(
                loader = { "value" },
                dispatcher = StandardTestDispatcher(),
                clock = MovableClock(),
                staleAfter = 100.milliseconds,
            )
        }
        assertTrue(failure.message.orEmpty().contains("backgroundScope"))
    }

    @Test
    fun `staleAfter beyond expireAfter is rejected at construction`() {
        assertFailsWith<IllegalArgumentException> {
            InMemoryCache<String, String>(
                loader = { "value" },
                dispatcher = StandardTestDispatcher(),
                clock = MovableClock(),
                staleAfter = 500.milliseconds,
                expireAfter = 100.milliseconds,
                backgroundScope = TestScope(),
            )
        }
    }
}

private fun TestScope.cacheOf(
    clock: Clock,
    scope: TestScope,
    staleAfter: kotlin.time.Duration? = null,
    expireAfter: kotlin.time.Duration? = null,
    loader: suspend (String) -> String,
): InMemoryCache<String, String> = InMemoryCache(
    loader = loader,
    dispatcher = StandardTestDispatcher(testScheduler),
    clock = clock,
    staleAfter = staleAfter,
    expireAfter = expireAfter,
    backgroundScope = scope,
)
