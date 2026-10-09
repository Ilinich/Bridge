package com.begoml.bridge.core.data.db

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.begoml.bridge.foundation.coroutines.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.module.Module
import java.io.File

internal class DesktopDatabaseFactory(private val queryDispatcher: CoroutineDispatcher) : DatabaseFactory {

    override fun create(): BridgeDatabase =
        Room.databaseBuilder<BridgeDatabase>(name = databasePath())
            .setDriver(BundledSQLiteDriver())
            // Everything stored here is re-fetchable, so a schema change drops the tables rather
            // than migrating them. Nothing a person typed is ever kept in this database.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .setQueryCoroutineContext(queryDispatcher)
            .build()

    /**
     * Where a desktop app is allowed to keep state: the OS data directory, not the working
     * directory, which is wherever the launcher happened to be.
     */
    private fun databasePath(): String {
        val home = File(System.getProperty("user.home"))
        val os = System.getProperty("os.name").lowercase()
        val base = when {
            os.contains("mac") -> File(home, "Library/Application Support")
            os.contains("win") -> File(System.getenv("APPDATA") ?: home.path)
            else -> File(System.getenv("XDG_DATA_HOME") ?: File(home, ".local/share").path)
        }
        val directory = File(base, "Bridge").apply { mkdirs() }
        return File(directory, DatabaseName).absolutePath
    }
}

actual fun platformStoreModule(): Module = roomStoreModule {
    single<DatabaseFactory> { DesktopDatabaseFactory(queryDispatcher = get<DispatcherProvider>().io) }
}
