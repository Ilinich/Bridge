package com.begoml.bridge.feature.matches.analytics

import com.begoml.bridge.core.analytics.AnalyticsEvent

/** What this feature reports. Nothing outside it needs these names. */
internal class MatchOpened(matchId: String) :
    AnalyticsEvent("match_opened", mapOf("match_id" to matchId))
