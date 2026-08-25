package com.begoml.bridge

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * The one place a dispatcher is named.
 *
 * Everything below this takes its dispatcher as a parameter, which is what keeps the cache and the
 * repositories deterministic under test; the composition root has to pick a real one somewhere.
 */
@Suppress("InjectDispatcher")
internal actual val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
