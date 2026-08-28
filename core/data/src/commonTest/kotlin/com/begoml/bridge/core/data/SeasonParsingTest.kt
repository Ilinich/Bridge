package com.begoml.bridge.core.data

import com.begoml.bridge.core.data.mapper.toSeason
import com.begoml.bridge.core.data.remote.BridgeJson
import com.begoml.bridge.core.data.remote.openfootball.SeasonEnvelope
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private val SeasonJson = """
{
  "name": "English Premier League 2025/26",
  "matches": [
    { "round": "Matchday 10", "date": "2025-10-25", "time": "15:00",
      "team1": "Chelsea FC", "team2": "Arsenal FC", "score": { "ft": [4, 2], "ht": [1, 0] } },
    { "round": "Matchday 2", "date": "2025-08-23", "time": "20:00",
      "team1": "Liverpool FC", "team2": "Chelsea FC", "score": [0, 0] },
    { "round": "Matchday 2", "date": "2025-08-23", "time": "14:00",
      "team1": "Everton FC", "team2": "Fulham FC" }
  ]
}
""".trimIndent()

class SeasonParsingTest {

    private fun season() = BridgeJson.decodeFromString<SeasonEnvelope>(SeasonJson).toSeason("2025-26")

    @Test
    fun `score is read from both the object form and the array form`() {
        val rounds = season().rounds.associateBy { it.number }

        val objectForm = rounds.getValue(10).matches.single()
        assertEquals(4, objectForm.score?.home)
        assertEquals(2, objectForm.score?.away)

        val arrayForm = rounds.getValue(2).matches.first { it.home.name == "Liverpool" }
        assertEquals(0, arrayForm.score?.home)
        assertEquals(0, arrayForm.score?.away)
    }

    @Test
    fun `a missing score is absent rather than nil-nil`() {
        val notPlayed = season().rounds
            .first { it.number == 2 }
            .matches
            .first { it.home.name == "Everton" }

        assertNull(notPlayed.score, "an unplayed fixture must not read as a goalless draw")
    }

    @Test
    fun `rounds are ordered by number rather than by the text they arrive as`() {
        val numbers = season().rounds.map { it.number }

        assertEquals(listOf(2, 10), numbers, "\"Matchday 10\" sorts before \"Matchday 2\" as text")
    }

    @Test
    fun `matches within a round are ordered by kickoff`() {
        val second = season().rounds.first { it.number == 2 }

        assertEquals(listOf("Everton", "Liverpool"), second.matches.map { it.home.name })
    }

    @Test
    fun `club names are reconciled between the two sources`() {
        val chelsea = season().rounds.first { it.number == 10 }.matches.single().home

        assertEquals("Chelsea", chelsea.name)
        assertEquals("CHE", chelsea.code)
    }
}
