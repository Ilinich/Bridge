package com.begoml.bridge.navigation.swipe

internal const val FreezeBackgroundWhileIdleKey = "freezeBackgroundWhileIdle"

object FreezeBackgroundWhileIdle {
    fun enabled(): Map<String, Any> = mapOf(FreezeBackgroundWhileIdleKey to true)
    fun isEnabledIn(metadata: Map<String, Any?>): Boolean =
        metadata[FreezeBackgroundWhileIdleKey] as? Boolean == true
}
