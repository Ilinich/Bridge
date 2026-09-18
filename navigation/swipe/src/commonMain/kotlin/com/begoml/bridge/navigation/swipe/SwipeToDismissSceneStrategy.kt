package com.begoml.bridge.navigation.swipe

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope

/**
 * Generic in the key type, because this module is not allowed to know what a destination is —
 * it works with whatever the host's back stack holds.
 */
class SwipeToDismissSceneStrategy<T : Any> : SceneStrategy<T> {

    override fun SceneStrategyScope<T>.calculateScene(
        entries: List<NavEntry<T>>,
    ): Scene<T>? {
        if (entries.size < 2) return null
        val currentEntry = entries.last()
        if (currentEntry.metadata[SwipeToDismissEnabledKey] != true) return null
        val previousEntry = entries[entries.size - 2]
        return SwipeToDismissScene(
            key = currentEntry.contentKey,
            previousEntry = previousEntry,
            currentEntry = currentEntry,
            previousEntries = entries.dropLast(1),
            freezeBackgroundWhileIdle = FreezeBackgroundWhileIdle.isEnabledIn(currentEntry.metadata),
            edgeWidthDp = SwipeEdgeGate.edgeWidthDp(currentEntry.metadata),
            swipeFromAnywhere = SwipeEdgeGate.isSwipeFromAnywhere(currentEntry.metadata),
            sensitivity = SwipeDismissSensitivity.from(currentEntry.metadata),
            onBack = onBack,
        )
    }

    companion object {
        internal const val SwipeToDismissEnabledKey = "swipeToDismissEnabled"
        fun enabled(): Map<String, Any> = mapOf(SwipeToDismissEnabledKey to true)
    }
}
