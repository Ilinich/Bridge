package com.begoml.bridge.core.data.db

import com.begoml.bridge.foundation.coroutines.DispatcherProvider
import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.module.Module
import org.koin.dsl.module

internal class AndroidDatabaseFactory(
    private val context: Context,
    private val queryDispatcher: CoroutineDispatcher,
) : DatabaseFactory {

    override fun create(): BridgeDatabase =
        Room.databaseBuilder<BridgeDatabase>(
            context = context.applicationContext,
            name = context.getDatabasePath(DatabaseName).absolutePath,
        )
            .setDriver(BundledSQLiteDriver())
            // Everything stored here is re-fetchable, so a schema change drops the tables rather
            // than migrating them. Nothing a person typed is ever kept in this database.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .setQueryCoroutineContext(queryDispatcher)
            .build()
}

actual fun platformDatabaseModule(): Module = module {
    single<DatabaseFactory> {
        AndroidDatabaseFactory(context = get<Context>(), queryDispatcher = get<DispatcherProvider>().io)
    }
}
