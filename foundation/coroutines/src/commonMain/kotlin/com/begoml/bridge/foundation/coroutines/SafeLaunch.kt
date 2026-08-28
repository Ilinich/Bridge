package com.begoml.bridge.foundation.coroutines

import com.begoml.bridge.foundation.logger.Logger
import com.begoml.bridge.foundation.logger.error
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Runs work off the thread that draws, and says so when it fails.
 *
 * Two things a plain `launch` does not do. It moves the work off the caller's context — a state
 * holder collects repositories and maps their answers, and neither belongs on the thread that
 * draws — onto whichever dispatcher it is handed, so a test hands it one it controls. And
 * it installs a handler, because an exception escaping a coroutine started with `launch` reaches
 * the platform's default handler — which on Android is a crash and on iOS is a line nobody reads.
 * Here it is logged with the tag of whatever started it, and the scope stays alive: one screen's
 * failed collection must not take the rest of the app down with it.
 *
 * Cancellation is not a failure and never reaches [onError] — the handler is not called for it.
 *
 * The scope must be a supervisor one, which is what [stateHolderScope] and [AppScope] both are.
 * Under a plain job a failing child hands its exception to the parent instead, the parent takes
 * the whole scope down, and the handler installed here is never consulted.
 *
 * @param onError runs after the line is logged, for a holder that wants to show the failure.
 */
fun CoroutineScope.safeLaunch(
    dispatcher: CoroutineDispatcher,
    logger: Logger,
    tag: String,
    onError: (Throwable) -> Unit = {},
    block: suspend CoroutineScope.() -> Unit,
): Job {
    val handler = CoroutineExceptionHandler { _, error ->
        logger.error(tag, "failed", error)
        onError(error)
    }
    return launch(dispatcher + handler, block = block)
}
