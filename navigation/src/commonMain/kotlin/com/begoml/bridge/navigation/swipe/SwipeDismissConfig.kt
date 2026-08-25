package com.begoml.bridge.navigation.swipe

import androidx.compose.runtime.Immutable

@Immutable
enum class SwipeSensitivity(val distanceFraction: Float, val velocityDp: Int) {
    Sensitive(distanceFraction = 0.20f, velocityDp = 800),
    Default(distanceFraction = 0.35f, velocityDp = 1500),
    Conservative(distanceFraction = 0.50f, velocityDp = 2000),
}

internal const val SWIPE_SENSITIVITY = "swipeSensitivity"

object SwipeDismissSensitivity {
    fun metadata(sensitivity: SwipeSensitivity): Map<String, Any> =
        if (sensitivity == SwipeSensitivity.Default) emptyMap() else mapOf(SWIPE_SENSITIVITY to sensitivity)

    fun from(metadata: Map<String, Any?>): SwipeSensitivity =
        metadata[SWIPE_SENSITIVITY] as? SwipeSensitivity ?: SwipeSensitivity.Default
}
