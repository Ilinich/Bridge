package com.begoml.bridge.core.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private const val RequestTimeoutMillis = 15_000L

internal val BridgeJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    explicitNulls = false
}

/**
 * One client for both sources.
 *
 * The JSON converter is registered for `text/plain` as well, because GitHub serves raw `.json`
 * files with that content type and negotiation matches on it — without this the season request
 * fails with a transformation error that names neither the URL nor the reason.
 */
fun createHttpClient(): HttpClient = HttpClient {
    install(ContentNegotiation) {
        json(BridgeJson)
        json(BridgeJson, contentType = ContentType.Text.Plain)
    }
    install(HttpTimeout) {
        requestTimeoutMillis = RequestTimeoutMillis
        connectTimeoutMillis = RequestTimeoutMillis
    }
}
