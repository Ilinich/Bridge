package com.begoml.bridge.navigation.swipe

internal const val FREEZE_BACKGROUND_WHILE_IDLE = "freezeBackgroundWhileIdle"

object FreezeBackgroundWhileIdle {
    fun enabled(): Map<String, Any> = mapOf(FREEZE_BACKGROUND_WHILE_IDLE to true)
    fun isEnabledIn(metadata: Map<String, Any?>): Boolean =
        metadata[FREEZE_BACKGROUND_WHILE_IDLE] as? Boolean == true
}
