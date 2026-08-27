package com.begoml.bridge.core.background.impl

import com.begoml.bridge.core.background.BackgroundRefresh
import com.begoml.bridge.core.background.RefreshWork
import com.begoml.bridge.foundation.logger.Logger
import com.begoml.bridge.foundation.logger.info
import com.begoml.bridge.foundation.logger.warn
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.module.Module
import platform.BackgroundTasks.BGAppRefreshTask
import platform.BackgroundTasks.BGAppRefreshTaskRequest
import platform.BackgroundTasks.BGTaskScheduler
import platform.Foundation.NSDate
import platform.Foundation.dateWithTimeIntervalSinceNow

private const val RefreshTag = "BackgroundRefresh"
private const val SecondsPerHour = 3600.0

internal actual fun Module.bindBackgroundRefresh() {
    single<BackgroundRefresh> { BgTaskRefresh(work = get(), logger = get()) }
}

/**
 * Submits a background-refresh request and re-submits after each run.
 *
 * iOS gives no schedule: a submitted request is permission for the system to run the task when it
 * decides to, and it may decline for days. The interval below is a floor, not a period. The task
 * also has to be declared in Info.plist under BGTaskSchedulerPermittedIdentifiers, or registration
 * throws at launch rather than failing quietly later.
 */
@OptIn(ExperimentalForeignApi::class)
private class BgTaskRefresh(
    private val work: RefreshWork,
    private val logger: Logger,
) : BackgroundRefresh {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun schedule() {
        BGTaskScheduler.sharedScheduler.registerForTaskWithIdentifier(
            identifier = RefreshTaskId,
            usingQueue = null,
        ) { task -> run(task as? BGAppRefreshTask) }
        submit()
    }

    private fun submit() {
        val request = BGAppRefreshTaskRequest(RefreshTaskId).apply {
            earliestBeginDate = NSDate.dateWithTimeIntervalSinceNow(
                RefreshIntervalHours * SecondsPerHour,
            )
        }
        val submitted = BGTaskScheduler.sharedScheduler.submitTaskRequest(request, null)
        if (submitted) {
            logger.info(RefreshTag, "submitted, earliest in $RefreshIntervalHours h")
        } else {
            logger.warn(RefreshTag, "the system refused the request")
        }
    }

    private fun run(task: BGAppRefreshTask?) {
        if (task == null) return
        // Re-submitted first: the run may be killed at any moment, and a request submitted after
        // the work would then never be made, leaving the app with no future refresh at all.
        submit()

        val job = scope.launch {
            val completed = work.run()
            task.setTaskCompletedWithSuccess(completed)
        }
        task.expirationHandler = { job.cancel() }
    }
}
