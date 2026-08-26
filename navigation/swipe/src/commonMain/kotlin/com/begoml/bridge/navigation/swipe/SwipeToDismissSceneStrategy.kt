package com.begoml.bridge.navigation.swipe

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope

class SwipeToDismissSceneStrategy : SceneStrategy<NavKey> {

    override fun SceneStrategyScope<NavKey>.calculateScene(
        entries: List<NavEntry<NavKey>>,
    ): Scene<NavKey>? {
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
