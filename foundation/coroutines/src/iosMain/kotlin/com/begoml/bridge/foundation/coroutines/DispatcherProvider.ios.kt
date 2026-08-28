package com.begoml.bridge.foundation.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * `Dispatchers.IO` is declared on Kotlin/Native but is `internal` there, so this is the widest
 * multi-threaded pool a caller can actually reach.
 *
 * It is not equivalent to the Android side, and the difference is worth knowing: `Dispatchers.IO`
 * grows to many threads, while `Default` is sized to the core count. Room queries run here and
 * SQLite blocks the calling thread, so enough concurrent queries would take threads away from
 * computation. This app issues few at a time; an app that issued many would want a dedicated pool.
 */
@Suppress("InjectDispatcher")
internal actual val platformIoDispatcher: CoroutineDispatcher = Dispatchers.Default
