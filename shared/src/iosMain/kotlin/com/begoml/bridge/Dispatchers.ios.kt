package com.begoml.bridge

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Kotlin/Native has no `Dispatchers.IO`; the default pool is the multi-threaded one here, and the
 * work behind it is HTTP rather than blocking file access.
 */
internal actual val ioDispatcher: CoroutineDispatcher = Dispatchers.Default
