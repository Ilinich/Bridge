package com.begoml.bridge.foundation.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * The browser has one thread, so this is the main thread under another name.
 *
 * Nothing here moves work off the thread that draws — a dispatcher cannot invent a worker the
 * platform does not have. What keeps the frame on web is that the work is asynchronous rather
 * than parallel: IndexedDB and `fetch` hand their results back through the event loop instead of
 * blocking, which is exactly why the web build stores through IndexedDB rather than through a
 * SQLite compiled to wasm, whose reads would block this thread the way they block a real one.
 */
@Suppress("InjectDispatcher")
internal actual val platformIoDispatcher: CoroutineDispatcher = Dispatchers.Default
