package com.begoml.bridge.core.background.impl

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.begoml.bridge.core.background.BackgroundRefresh
import com.begoml.bridge.core.background.RefreshWork
import com.begoml.bridge.foundation.logger.Logger
import com.begoml.bridge.foundation.logger.info
import org.koin.android.ext.koin.androidContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.module.Module
import java.util.concurrent.TimeUnit

private const val RefreshTag = "BackgroundRefresh"

internal actual fun Module.bindBackgroundRefresh() {
    single<BackgroundRefresh> { WorkManagerRefresh(androidContext(), get()) }
}

private class WorkManagerRefresh(
    private val context: Context,
    private val logger: Logger,
) : BackgroundRefresh {

    override fun schedule() {
        val request = PeriodicWorkRequestBuilder<RefreshWorker>(RefreshIntervalHours, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build(),
            )
            .build()

        // UPDATE rather than KEEP or REPLACE. REPLACE would restart the period on every launch,
        // so on a phone opened daily the work would never come due. KEEP has a subtler failure:
        // the enqueued record stores the worker's class name, so a class that moves package —
        // as this one did — leaves every existing install pointing at a name that no longer
        // resolves, with no way to correct it. UPDATE rewrites the record and keeps the schedule.
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            RefreshTaskId,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
        logger.info(RefreshTag, "scheduled every $RefreshIntervalHours h")
    }
}

/**
 * Runs the shared refresh.
 *
 * The work is resolved from the graph rather than injected: WorkManager constructs workers itself,
 * and there is no composition here to resolve from.
 */
internal class RefreshWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params), KoinComponent {

    private val work: RefreshWork by inject()
    private val logger: Logger by inject()

    override suspend fun doWork(): Result {
        logger.info(RefreshTag, "running")
        return if (work.run()) Result.success() else Result.retry()
    }
}
