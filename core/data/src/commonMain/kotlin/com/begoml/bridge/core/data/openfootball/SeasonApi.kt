package com.begoml.bridge.core.data.openfootball

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode

private const val SeasonUrlTemplate =
    "https://raw.githubusercontent.com/openfootball/football.json/master/%s/en.1.json"

/**
 * A whole Premier League season in one file.
 *
 * All 380 fixtures arrive in a single 108 KB response, so paging through rounds afterwards costs
 * no network at all. Returns null when that season is not published yet, which is the normal
 * state of a season in the days before it opens.
 */
internal class SeasonApi(private val client: HttpClient) {

    suspend fun season(seasonId: String): SeasonEnvelope? {
        val response: HttpResponse = client.get(SeasonUrlTemplate.withSeason(seasonId))
        if (response.status == HttpStatusCode.NotFound) return null
        return response.body()
    }
}

/** The template carries one `%s`; this is a substitution, not a printf-style format. */
private fun String.withSeason(seasonId: String): String = replace("%s", seasonId)
