package com.begoml.bridge.navigation.swipe

import androidx.compose.ui.unit.Dp

internal const val SwipeEdgeWidthDpKey = "swipeEdgeWidthDp"
internal const val SwipeFromAnywhereKey = "swipeFromAnywhere"

object SwipeEdgeGate {
    fun metadata(edgeWidthDp: Dp?, swipeFromAnywhere: Boolean): Map<String, Any> = buildMap {
        if (edgeWidthDp != null) put(SwipeEdgeWidthDpKey, edgeWidthDp.value)
        if (swipeFromAnywhere) put(SwipeFromAnywhereKey, true)
    }

    fun edgeWidthDp(metadata: Map<String, Any?>): Dp? =
        (metadata[SwipeEdgeWidthDpKey] as? Float)?.let { Dp(it) }

    fun isSwipeFromAnywhere(metadata: Map<String, Any?>): Boolean =
        metadata[SwipeFromAnywhereKey] as? Boolean == true
}
