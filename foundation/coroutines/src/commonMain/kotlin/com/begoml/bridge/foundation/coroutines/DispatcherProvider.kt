package com.begoml.bridge.foundation.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * The dispatchers the app is allowed to name.
 *
 * Everything below the composition root takes the one it needs as a parameter, which is what keeps
 * repositories and caches deterministic under test — a hard-coded `Dispatchers.IO` cannot be
 * replaced by a test scheduler, and a test that has to wait for a real thread pool is a test that
 * will be flaky one day.
 */
interface DispatcherProvider {

    /** Blocking work: sockets, files, SQLite. */
    val io: CoroutineDispatcher

    /** Computation: mapping, sorting, parsing. */
    val default: CoroutineDispatcher

    /** The thread that draws. */
    val main: CoroutineDispatcher
}

internal expect val platformIoDispatcher: CoroutineDispatcher

@Suppress("InjectDispatcher")
class PlatformDispatcherProvider : DispatcherProvider {

    override val io: CoroutineDispatcher = platformIoDispatcher

    override val default: CoroutineDispatcher = Dispatchers.Default

    override val main: CoroutineDispatcher = Dispatchers.Main.immediate
}
