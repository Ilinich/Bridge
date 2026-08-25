package com.begoml.bridge.core.data.db

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.CoroutineDispatcher
import com.begoml.bridge.core.data.di.IoDispatcher
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
    single<DatabaseFactory> { IosDatabaseFactory(queryDispatcher = get(IoDispatcher)) }
}
