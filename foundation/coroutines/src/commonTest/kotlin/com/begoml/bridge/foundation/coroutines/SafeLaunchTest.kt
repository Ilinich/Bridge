package com.begoml.bridge.foundation.coroutines

import com.begoml.bridge.foundation.logger.LogLevel
import com.begoml.bridge.foundation.logger.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class RecordingLogger : Logger {

    val errors = mutableListOf<Pair<String, Throwable?>>()

    override fun log(level: LogLevel, tag: String, message: String, error: Throwable?) {
        if (level == LogLevel.Error) errors += tag to error
    }
}

private class Boom : RuntimeException("boom")

class SafeLaunchTest {

    @Test
    fun `a failure is logged and the scope survives it`() = runTest {
        val logger = RecordingLogger()
        // A supervisor job, like the scope a state holder is given: without one a failing child
        // hands its exception to the parent, and the handler installed here is never consulted.
        val scope = CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler))

        scope.safeLaunch(
            dispatcher = StandardTestDispatcher(testScheduler),
            logger = logger,
            tag = "Screen",
        ) { throw Boom() }
        advanceUntilIdle()

        assertEquals(1, logger.errors.size)
        assertEquals("Screen", logger.errors.single().first)
        assertTrue(logger.errors.single().second is Boom)
        assertTrue(scope.isActive)
    }

    @Test
    fun `cancelling the work is not reported as a failure`() = runTest {
        val logger = RecordingLogger()
        val scope = CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler))

        val job = scope.safeLaunch(
            dispatcher = StandardTestDispatcher(testScheduler),
            logger = logger,
            tag = "Screen",
        ) {
            throw CancellationException("gone")
        }
        advanceUntilIdle()

        assertTrue(job.isCancelled)
        assertEquals(emptyList(), logger.errors)
    }

    @Test
    fun `the work runs on the dispatcher it was handed, not the caller's`() = runTest {
        val logger = RecordingLogger()
        val handed = StandardTestDispatcher(testScheduler)
        val scope = CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler))
        var ranOn: CoroutineDispatcher? = null

        scope.safeLaunch(dispatcher = handed, logger = logger, tag = "Screen") {
            @OptIn(kotlin.ExperimentalStdlibApi::class)
            ranOn = currentCoroutineContext()[CoroutineDispatcher]
        }
        advanceUntilIdle()

        assertEquals(handed, ranOn)
    }
}
