package com.begoml.bridge.core.data.openfootball

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

private const val SeasonUrl =
    "https://raw.githubusercontent.com/openfootball/football.json/master/2025-26/en.1.json"

/**
 * The whole Premier League season in one file.
 *
 * All 380 fixtures arrive in a single 108 KB response, so paging through rounds afterwards costs
 * no network at all. That is why this source exists alongside TheSportsDB, whose free tier returns
 * five fixtures for the same question.
 */
internal class SeasonApi(private val client: HttpClient) {

    suspend fun season(): SeasonEnvelope = client.get(SeasonUrl).body()
}
