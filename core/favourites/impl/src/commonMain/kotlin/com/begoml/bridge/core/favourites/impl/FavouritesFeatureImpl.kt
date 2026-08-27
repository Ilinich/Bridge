package com.begoml.bridge.core.favourites.impl

import com.begoml.bridge.core.favourites.FavouritesFeature
import com.begoml.bridge.core.favourites.FavouritesState
import com.begoml.bridge.foundation.tessera.SimpleFeature
import com.begoml.bridge.foundation.tessera.awaitActionsIn
import com.begoml.bridge.foundation.tessera.feature
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal sealed interface FavouritesAction {

    data class Toggle(val playerId: String) : FavouritesAction
}

internal sealed interface FavouritesEvent

/**
 * Follows the store rather than leading it.
 *
 * A toggle writes and returns; the new set arrives back through [FavouritesStore.observe] like any
 * other change, so the screen can never show a set that failed to persist. The write is small
 * enough that the round trip lands inside the same frame in practice.
 */
internal class FavouritesFeatureImpl(
    scope: CoroutineScope,
    private val store: FavouritesStore,
) : FavouritesFeature,
    SimpleFeature<FavouritesState, FavouritesAction, FavouritesEvent>
    by feature(FavouritesState(), scope) {

    init {
        scope.launch {
            store.observe().collect { stored ->
                updateState { FavouritesState(playerIds = stored.toImmutableSet(), isLoaded = true) }
            }
        }
        awaitActionsIn(scope) { action ->
            when (action) {
                is FavouritesAction.Toggle -> store.toggle(action.playerId)
            }
        }
    }

    override fun toggle(playerId: String) {
        dispatchAction(FavouritesAction.Toggle(playerId))
    }
}
