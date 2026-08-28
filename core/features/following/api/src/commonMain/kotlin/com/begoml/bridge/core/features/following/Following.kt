package com.begoml.bridge.core.features.following

import com.begoml.bridge.foundation.tessera.FeatureStateDelegate
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf

/**
 * The players this person follows.
 *
 * [isLoaded] separates "nobody is followed" from "the store has not answered yet", so a screen
 * does not draw an empty set as a decision the person made.
 */
data class FollowingState(
    val playerIds: ImmutableSet<String> = persistentSetOf(),
    val isLoaded: Boolean = false,
) {

    fun contains(playerId: String): Boolean = playerId in playerIds
}

/**
 * A state holder with no screen of its own.
 *
 * The squad grid marks them, the player pager writes them and matchday reads them, so the set
 * cannot belong to any one of those screens: it outlives all of them and is shared between two
 * feature modules that may not depend on each other. It survives the process, so it is the store
 * on disk that is authoritative and this is a view of it.
 */
interface FollowingFeature : FeatureStateDelegate<FollowingState> {

    /**
     * Follows the player, or stops following them if they already were.
     *
     * Returns immediately and does not report the outcome: the write goes to the store, and the
     * new set comes back through [stateFlow] like any other change, so nothing can be shown that
     * failed to persist. An id the current squad no longer contains is kept but goes unnamed
     * wherever names are needed.
     */
    fun toggle(playerId: String)
}
