package com.begoml.bridge.core.analytics

/**
 * Records what the user did, not what the code did.
 *
 * The whole contract is this interface and [AnalyticsEvent]. Events themselves are declared by the
 * feature that reports them: what a squad screen records is squad vocabulary, and collecting every
 * event here would make this module a place every feature has to edit — and a place that has to
 * know what a player and a fixture are.
 */
interface Analytics {

    /**
     * Records that something happened.
     *
     * Fire and forget: it never blocks the caller, never throws and never reports delivery — a
     * screen must not behave differently because analytics was unavailable.
     */
    fun track(event: AnalyticsEvent)
}

/**
 * One recorded action.
 *
 * [name] is the wire form and is expected to stay stable; [params] carries identifiers, never
 * anything that identifies a person.
 */
abstract class AnalyticsEvent(
    val name: String,
    val params: Map<String, String> = emptyMap(),
)
