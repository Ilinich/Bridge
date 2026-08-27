package com.begoml.bridge.foundation.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Suppress("InjectDispatcher")
internal actual val platformIoDispatcher: CoroutineDispatcher = Dispatchers.IO
