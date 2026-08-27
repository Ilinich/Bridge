package com.begoml.bridge.core.data.db

import com.begoml.bridge.foundation.coroutines.DispatcherProvider
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

internal class IosDatabaseFactory(private val queryDispatcher: CoroutineDispatcher) : DatabaseFactory {

    override fun create(): BridgeDatabase =
        Room.databaseBuilder<BridgeDatabase>(name = databasePath())
            .setDriver(BundledSQLiteDriver())
            // Everything stored here is re-fetchable, so a schema change drops the tables rather
            // than migrating them. Nothing a person typed is ever kept in this database.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .setQueryCoroutineContext(queryDispatcher)
            .build()

    @OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
    private fun databasePath(): String {
        val documents: NSURL? = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
        return requireNotNull(documents?.path) { "No documents directory" } + "/" + DatabaseName
    }
}

actual fun platformDatabaseModule(): Module = module {
    single<DatabaseFactory> { IosDatabaseFactory(queryDispatcher = get<DispatcherProvider>().io) }
}
