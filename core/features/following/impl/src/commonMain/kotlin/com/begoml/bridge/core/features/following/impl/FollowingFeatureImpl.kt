package com.begoml.bridge.core.features.following.impl

import com.begoml.bridge.core.features.following.FollowingFeature
import com.begoml.bridge.core.features.following.FollowingState
import com.begoml.bridge.foundation.tessera.FeaturePlugin
import com.begoml.bridge.foundation.tessera.Feature
import com.begoml.bridge.foundation.tessera.awaitActionsIn
import com.begoml.bridge.foundation.tessera.feature
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal sealed interface FollowingAction {

    data class Toggle(val playerId: String) : FollowingAction
}

/**
 * Follows the store rather than leading it.
 *
 * A toggle writes and returns; the new set arrives back through [FollowingStore.observe] like any
 * other change, so the screen can never show a set that failed to persist. The write is small
 * enough that the round trip lands inside the same frame in practice.
 */
internal class FollowingFeatureImpl(
    scope: CoroutineScope,
    private val store: FollowingStore,
    plugins: List<FeaturePlugin<FollowingState, FollowingAction>> = emptyList(),
) : FollowingFeature,
    Feature<FollowingState, FollowingAction>
    by feature(initialState = FollowingState(), scope = scope, plugins = plugins) {

    init {
        scope.launch {
            store.observe().collect { stored ->
                updateState { FollowingState(playerIds = stored, isLoaded = true) }
            }
        }
        awaitActionsIn(scope) { action ->
            when (action) {
                is FollowingAction.Toggle -> store.toggle(action.playerId)
            }
        }
    }

    override fun toggle(playerId: String) {
        dispatchAction(FollowingAction.Toggle(playerId))
    }
}
