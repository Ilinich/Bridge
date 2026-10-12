package com.begoml.bridge.core.features.following.impl

import kotlinx.browser.localStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.module.Module

private const val Key = "bridge.following"
private const val Separator = ","

/**
 * The followed ids in the origin's own storage.
 *
 * `localStorage` rather than the IndexedDB the feeds use: this is a handful of ids written when
 * somebody taps a star, and a synchronous write of a short string costs less than a transaction.
 * It is also the one thing in the app a person authored, so it is kept apart from the cache that
 * is dropped whenever the stored shape changes.
 *
 * Every access is guarded: a browser in private mode, or one with site data blocked, throws from
 * the accessor rather than returning null, and a star that cannot be remembered must not take the
 * screen with it.
 */
internal class LocalStorageFollowing : FollowingStore {

    private val state = MutableStateFlow(read())

    override fun observe(): Flow<Set<String>> = state.asStateFlow()

    override suspend fun toggle(playerId: String) {
        val current = state.value
        val next = if (playerId in current) current - playerId else current + playerId
        state.value = next
        runCatching { localStorage.setItem(Key, next.joinToString(Separator)) }
    }

    private fun read(): Set<String> = runCatching {
        localStorage.getItem(Key)
            ?.split(Separator)
            ?.filter { it.isNotBlank() }
            ?.toSet()
    }.getOrNull().orEmpty()
}

internal actual fun Module.bindFollowingStore() {
    single<FollowingStore> { LocalStorageFollowing() }
}
