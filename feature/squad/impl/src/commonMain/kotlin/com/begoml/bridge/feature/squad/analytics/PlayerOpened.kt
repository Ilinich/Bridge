package com.begoml.bridge.feature.squad.analytics

import com.begoml.bridge.core.analytics.AnalyticsEvent

/** What this feature reports. Nothing outside it needs these names. */
internal class PlayerOpened(playerId: String) :
    AnalyticsEvent("player_opened", mapOf("player_id" to playerId))
