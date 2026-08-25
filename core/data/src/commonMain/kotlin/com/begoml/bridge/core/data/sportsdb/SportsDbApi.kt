package com.begoml.bridge.core.data.sportsdb

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

private const val BaseUrl = "https://www.thesportsdb.com/api/v1/json/3"

/**
 * TheSportsDB on its free test key.
 *
 * The tier truncates silently with HTTP 200 — ten players instead of a squad, one past match
 * instead of a run of them. A short list is a valid answer here, never an error, so nothing in
 * this class treats emptiness as a failure.
 */
internal class SportsDbApi(private val client: HttpClient) {

    suspend fun team(teamId: String): TeamDto? =
        client.get("$BaseUrl/lookupteam.php?id=$teamId").body<TeamsEnvelope>().teams?.firstOrNull()

    suspend fun nextEvents(teamId: String): List<EventDto> =
        client.get("$BaseUrl/eventsnext.php?id=$teamId").body<NextEventsEnvelope>().events.orEmpty()

    suspend fun lastEvents(teamId: String): List<EventDto> =
        client.get("$BaseUrl/eventslast.php?id=$teamId").body<LastEventsEnvelope>().results.orEmpty()

    suspend fun squad(teamId: String): List<PlayerDto> =
        client.get("$BaseUrl/lookup_all_players.php?id=$teamId").body<PlayersEnvelope>().players.orEmpty()
}
