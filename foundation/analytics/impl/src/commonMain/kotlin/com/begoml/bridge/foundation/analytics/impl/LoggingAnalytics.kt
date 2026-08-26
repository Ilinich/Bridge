package com.begoml.bridge.foundation.analytics.impl

import com.begoml.bridge.foundation.analytics.Analytics
import com.begoml.bridge.foundation.analytics.AnalyticsEvent
import com.begoml.bridge.foundation.logger.Logger
import com.begoml.bridge.foundation.logger.info

private const val AnalyticsTag = "Analytics"

/**
 * Prints events instead of sending them.
 *
 * There is no vendor SDK in this project, and this is the point of the split rather than a
 * shortcut: features record events against [Analytics], so adding a real backend is a change to
 * this module alone and to nothing that calls it.
 */
internal class LoggingAnalytics(private val logger: Logger) : Analytics {

    override fun track(event: AnalyticsEvent) {
        val params = event.params
            .takeIf { it.isNotEmpty() }
            ?.entries
            ?.joinToString(prefix = " ") { (key, value) -> "$key=$value" }
            .orEmpty()
        logger.info(AnalyticsTag, event.name + params)
    }
}
