package com.begoml.bridge.foundation.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/** The JVM has the real thing, and SQLite on the desktop blocks a thread the way it does on Android. */
@Suppress("InjectDispatcher")
internal actual val platformIoDispatcher: CoroutineDispatcher = Dispatchers.IO
