package com.begoml.bridge.core.data

import com.begoml.bridge.core.data.model.Season
import com.begoml.bridge.core.data.model.SeasonMatch
import com.begoml.bridge.core.data.model.SeasonRound
import com.begoml.bridge.core.data.model.TeamRef
import com.begoml.bridge.core.data.model.roundAt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

private fun round(number: Int, vararg kickoffMillis: Long) = SeasonRound(
    number = number,
    matches = kickoffMillis.map { millis ->
        SeasonMatch(
            id = "$number-$millis",
            round = number,
            kickoff = Instant.fromEpochMilliseconds(millis),
            home = TeamRef("Home", "HOM", null),
            away = TeamRef("Away", "AWA", null),
            score = null,
        )
    },
)

class SeasonRoundTest {

    private val season = Season(
        name = "test",
        rounds = listOf(round(1, 100L), round(2, 200L, 300L), round(3, 400L)),
    )

    @Test
    fun `the calendar opens on the first round with a fixture still ahead`() {
        assertEquals(2, season.roundAt(nowMillis = 150L)?.number)
        assertEquals(2, season.roundAt(nowMillis = 250L)?.number)
    }

    @Test
    fun `before the season starts the first round is chosen`() {
        assertEquals(1, season.roundAt(nowMillis = 0L)?.number)
    }

    @Test
    fun `after the last fixture the final round is chosen rather than the first`() {
        assertEquals(3, season.roundAt(nowMillis = 9_000L)?.number)
    }

    @Test
    fun `an empty season has no round`() {
        assertNull(Season(name = "empty", rounds = emptyList()).roundAt(nowMillis = 0L))
    }
}
