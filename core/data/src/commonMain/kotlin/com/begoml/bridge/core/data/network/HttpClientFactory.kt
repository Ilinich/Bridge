package com.begoml.bridge.core.data.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private const val RequestTimeoutMillis = 15_000L

internal val BridgeJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    explicitNulls = false
}

fun createHttpClient(): HttpClient = HttpClient {
    install(ContentNegotiation) { json(BridgeJson) }
    install(HttpTimeout) {
        requestTimeoutMillis = RequestTimeoutMillis
        connectTimeoutMillis = RequestTimeoutMillis
    }
}
