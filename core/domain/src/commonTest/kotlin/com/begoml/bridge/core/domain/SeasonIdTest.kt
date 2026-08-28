package com.begoml.bridge.core.domain

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals

private fun millisOf(date: String): Long =
    LocalDateTime(LocalDate.parse(date), LocalTime(12, 0)).toInstant(TimeZone.UTC).toEpochMilliseconds()

class SeasonIdTest {

    @Test
    fun `august opens the season named after that year`() {
        assertEquals("2026-27", seasonIdAt(millisOf("2026-08-01")))
        assertEquals("2026-27", seasonIdAt(millisOf("2026-08-25")))
    }

    @Test
    fun `july still belongs to the season that opened the year before`() {
        assertEquals("2025-26", seasonIdAt(millisOf("2026-07-31")))
    }

    @Test
    fun `spring belongs to the season that opened the previous august`() {
        assertEquals("2026-27", seasonIdAt(millisOf("2027-05-30")))
        assertEquals("2026-27", seasonIdAt(millisOf("2027-01-02")))
    }

    @Test
    fun `the end year keeps two digits across a century boundary`() {
        assertEquals("2099-00", seasonIdAt(millisOf("2099-09-01")))
    }

    @Test
    fun `the previous season is the one that opened a year earlier`() {
        assertEquals("2025-26", previousSeasonId("2026-27"))
        assertEquals("2098-99", previousSeasonId("2099-00"))
    }
}
