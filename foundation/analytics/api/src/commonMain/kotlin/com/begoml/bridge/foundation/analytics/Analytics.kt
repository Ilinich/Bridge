package com.begoml.bridge.foundation.analytics

/**
 * Records what the user did, not what the code did.
 *
 * Events are declared as types rather than passed as free strings: a typo then fails to compile
 * instead of arriving in a dashboard as a second, slightly different event that nobody notices.
 */
interface Analytics {

    fun track(event: AnalyticsEvent)
}

/**
 * One recorded action.
 *
 * [name] is the wire form and is expected to stay stable; [params] carries identifiers, never
 * anything that identifies a person.
 */
sealed class AnalyticsEvent(val name: String, val params: Map<String, String> = emptyMap()) {

    data object AppOpened : AnalyticsEvent("app_opened")

    class TabSelected(tab: String) : AnalyticsEvent("tab_selected", mapOf("tab" to tab))

    class PlayerOpened(playerId: String) :
        AnalyticsEvent("player_opened", mapOf("player_id" to playerId))

    class MatchOpened(matchId: String) :
        AnalyticsEvent("match_opened", mapOf("match_id" to matchId))

    class VideoStarted(source: String) :
        AnalyticsEvent("video_started", mapOf("source" to source))
}
