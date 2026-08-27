package com.begoml.bridge.analytics

import com.begoml.bridge.core.analytics.AnalyticsEvent

/** What the host reports: the app itself, and moving between its sections. */
internal data object AppOpened : AnalyticsEvent("app_opened")

internal class TabSelected(tab: String) : AnalyticsEvent("tab_selected", mapOf("tab" to tab))
