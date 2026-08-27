package com.begoml.bridge.feature.club.analytics

import com.begoml.bridge.core.analytics.AnalyticsEvent

/** What this feature reports. Nothing outside it needs these names. */
internal class VideoStarted(source: String) :
    AnalyticsEvent("video_started", mapOf("source" to source))
