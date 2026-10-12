package com.begoml.bridge.core.data.db

internal const val DatabaseName = "bridge.db"

interface DatabaseFactory {

    fun create(): BridgeDatabase
}
