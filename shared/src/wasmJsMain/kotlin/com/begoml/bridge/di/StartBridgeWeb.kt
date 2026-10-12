package com.begoml.bridge.di

import com.begoml.bridge.core.data.db.openWebStorage
import org.koin.core.Koin

/**
 * Starts the graph with storage already open and read.
 *
 * The browser's database opens asynchronously, and a screen that asked for its data while it was
 * still opening would have to decide what "nothing stored yet" means — which is the same question
 * as "nothing stored at all", answered differently. Doing it here removes the question: by the
 * time anything is composed, the mirror holds what the last visit left.
 */
suspend fun startBridgeWeb(): Koin {
    openWebStorage()
    return startBridge().koin
}
