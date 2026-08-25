package com.begoml.bridge.core.data.db

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.CoroutineDispatcher
import com.begoml.bridge.core.data.di.IoDispatcher
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
            .setQueryCoroutineContext(queryDispatcher)
            .build()
}

actual fun platformDatabaseModule(): Module = module {
    single<DatabaseFactory> {
        AndroidDatabaseFactory(context = get<Context>(), queryDispatcher = get(IoDispatcher))
    }
}
