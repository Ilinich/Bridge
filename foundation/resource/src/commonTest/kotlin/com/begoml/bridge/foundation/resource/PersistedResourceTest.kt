package com.begoml.bridge.foundation.resource

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val NothingElseMillis = 1_000L

private class Boom : RuntimeException("the network is down")

class PersistedResourceTest {

    @Test
    fun `what is stored is reported without waiting for the network`() = runTest {
        val emissions = persistedResource(stored = flowOf("cached")) { }.take(2).toList()

        assertEquals(listOf(Loadable.Loading, Loadable.Content("cached")), emissions)
    }

    @Test
    fun `a failed fetch is reported only when there is nothing stored`() = runTest {
        val emissions = persistedResource<String>(stored = flowOf(null)) { throw Boom() }
            .take(2)
            .toList()

        assertEquals(Loadable.Loading, emissions.first())
        assertTrue(emissions.last() is Loadable.Failed)
    }

    @Test
    fun `a failed refresh leaves a stored value standing`() = runTest {
        // A third emission would be the failure replacing content the screen is already showing.
        val third = withTimeoutOrNull(NothingElseMillis) {
            persistedResource(stored = flowOf("cached")) { throw Boom() }.take(3).toList()
        }

        assertNull(third)
    }
}
