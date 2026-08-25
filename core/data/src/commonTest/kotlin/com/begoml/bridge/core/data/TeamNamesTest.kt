package com.begoml.bridge.core.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TeamNamesTest {

    @Test
    fun `the same club written two ways is one club`() {
        assertTrue(TeamNames.matches("Chelsea FC", "Chelsea"))
        assertTrue(TeamNames.matches("Manchester United FC", "Manchester United"))
    }

    @Test
    fun `an unknown club is degraded to initials rather than dropped`() {
        assertEquals("REA", TeamNames.code("Real Sociedad"))
        assertEquals("Real Sociedad", TeamNames.displayName("Real Sociedad"))
    }

    @Test
    fun `a known club keeps its established code`() {
        assertEquals("CHE", TeamNames.code("Chelsea FC"))
        assertEquals("NFO", TeamNames.code("Nottingham Forest"))
    }
}
