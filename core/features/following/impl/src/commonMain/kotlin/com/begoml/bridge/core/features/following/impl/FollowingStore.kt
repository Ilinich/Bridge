package com.begoml.bridge.core.features.following.impl

import kotlinx.coroutines.flow.Flow

/**
 * The set the person authored.
 *
 * Separate from the cached feeds on purpose: those are re-fetchable and are dropped wholesale on
 * a schema change, while this is the only thing in the app somebody chose. Where it is kept is
 * the platform's business — a preferences file where there is a filesystem, the origin's own
 * storage in a browser.
 */
internal interface FollowingStore {

    fun observe(): Flow<Set<String>>

    suspend fun toggle(playerId: String)
}
