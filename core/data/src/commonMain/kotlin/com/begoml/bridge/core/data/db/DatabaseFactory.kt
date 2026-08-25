package com.begoml.bridge.core.data.db

import org.koin.core.module.Module

internal const val DatabaseName = "bridge.db"

interface DatabaseFactory {

    fun create(): BridgeDatabase
}

/** Binds the platform's way of locating and opening the database file. */
expect fun platformDatabaseModule(): Module
