package com.begoml.bridge.foundation.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/**
 * Work that belongs to the app rather than to anything on screen.
 *
 * A background refresh has no composition, no ViewModel and no screen to be owned by — it is
 * started by the platform while the app may not even be visible — so the alternative to this is
 * every such caller inventing a scope of its own, off in platform code where no test can reach it.
 *
 * A distinct type rather than a qualified `CoroutineScope`: the qualifier is checked at run time
 * and its absence shows up as a crash on a code path that runs once a day.
 *
 * [SupervisorJob] because these launches are unrelated: one failing run must not take the scope
 * down with it and leave the process with nothing to launch from until it is restarted. Nothing
 * cancels this scope — it ends when the process does.
 */
class AppScope(dispatcher: CoroutineDispatcher) :
    CoroutineScope by CoroutineScope(SupervisorJob() + dispatcher)
