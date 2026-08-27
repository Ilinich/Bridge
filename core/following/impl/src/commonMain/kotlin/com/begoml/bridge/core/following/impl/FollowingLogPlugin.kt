package com.begoml.bridge.core.following.impl

import com.begoml.bridge.core.following.FollowingState
import com.begoml.bridge.foundation.logger.Logger
import com.begoml.bridge.foundation.logger.info
import com.begoml.bridge.foundation.tessera.FeaturePlugin

private const val Tag = "Following"

/**
 * Says who was followed and who was dropped, and nothing else.
 *
 * It reads transitions rather than actions on purpose: a toggle is only a request, and what
 * actually happened is decided by the store — a write that failed produces no transition and so
 * produces no line claiming it succeeded.
 *
 * The first transition is skipped. That one is the store answering on start-up, and reporting a
 * restored set as if the person had just picked it would make every launch look like a decision.
 */
internal class FollowingLogPlugin(
    private val logger: Logger,
) : FeaturePlugin<FollowingState, FollowingAction, FollowingEvent> {

    override fun onState(old: FollowingState, new: FollowingState) {
        if (!old.isLoaded) return
        (new.playerIds - old.playerIds).forEach { id -> logger.info(Tag, "followed $id") }
        (old.playerIds - new.playerIds).forEach { id -> logger.info(Tag, "unfollowed $id") }
    }
}
