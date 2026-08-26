package com.begoml.bridge.navigation.swipe

import androidx.compose.ui.unit.Dp

internal const val SWIPE_EDGE_WIDTH_DP = "swipeEdgeWidthDp"
internal const val SWIPE_FROM_ANYWHERE = "swipeFromAnywhere"

object SwipeEdgeGate {
    fun metadata(edgeWidthDp: Dp?, swipeFromAnywhere: Boolean): Map<String, Any> = buildMap {
        if (edgeWidthDp != null) put(SWIPE_EDGE_WIDTH_DP, edgeWidthDp.value)
        if (swipeFromAnywhere) put(SWIPE_FROM_ANYWHERE, true)
    }

    fun edgeWidthDp(metadata: Map<String, Any?>): Dp? =
        (metadata[SWIPE_EDGE_WIDTH_DP] as? Float)?.let { Dp(it) }

    fun isSwipeFromAnywhere(metadata: Map<String, Any?>): Boolean =
        metadata[SWIPE_FROM_ANYWHERE] as? Boolean == true
}
