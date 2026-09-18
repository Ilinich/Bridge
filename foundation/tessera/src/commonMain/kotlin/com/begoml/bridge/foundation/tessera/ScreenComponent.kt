package com.begoml.bridge.foundation.tessera

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel

/**
 * What a platform UI holds when the platform has nowhere to put a state holder.
 *
 * Android has a ViewModelStore and something that empties it; SwiftUI has neither, so the thing a
 * Swift view holds has to say when it is done. The scope is the one the state holder runs on,
 * which is why closing this ends its work rather than merely dropping a reference.
 */
abstract class ScreenComponent(private val scope: CoroutineScope) {

    fun close() {
        scope.cancel()
    }
}
