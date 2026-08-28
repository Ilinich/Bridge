package com.begoml.bridge.feature.matches.matchday

import bridge.feature.matches.impl.generated.resources.Res
import bridge.feature.matches.impl.generated.resources.fixture_versus
import bridge.feature.matches.impl.generated.resources.matchday_arena
import bridge.feature.matches.impl.generated.resources.matchday_capacity
import bridge.feature.matches.impl.generated.resources.matchday_days
import bridge.feature.matches.impl.generated.resources.matchday_fixture_failed
import bridge.feature.matches.impl.generated.resources.matchday_following
import bridge.feature.matches.impl.generated.resources.matchday_founded
import bridge.feature.matches.impl.generated.resources.matchday_hours
import bridge.feature.matches.impl.generated.resources.matchday_kickoff_local
import bridge.feature.matches.impl.generated.resources.matchday_kickoff_now
import bridge.feature.matches.impl.generated.resources.matchday_loading_fixture
import bridge.feature.matches.impl.generated.resources.matchday_minutes
import bridge.feature.matches.impl.generated.resources.matchday_next_match
import bridge.feature.matches.impl.generated.resources.matchday_no_fixture
import bridge.feature.matches.impl.generated.resources.matchday_recent
import bridge.feature.matches.impl.generated.resources.matchday_seconds
import bridge.feature.matches.impl.generated.resources.matchday_stadium
import com.begoml.bridge.foundation.strings.LabelsLoader
import com.begoml.bridge.foundation.strings.StringResolver

/**
 * This feature's fixed words, read once and held for the run.
 *
 * The ids are the feature's own; how they are turned into words is not its business, which is why
 * a resolver is injected rather than a reader imported.
 */
class MatchdayLabelsSource(private val strings: StringResolver) : LabelsLoader {

    lateinit var labels: MatchdayLabels
        private set

    override suspend fun load() {
        labels = MatchdayLabels(
            nextMatch = strings.get(Res.string.matchday_next_match),
            fixtureFailed = strings.get(Res.string.matchday_fixture_failed),
            noFixture = strings.get(Res.string.matchday_no_fixture),
            loadingFixture = strings.get(Res.string.matchday_loading_fixture),
            kickoffLocal = strings.get(Res.string.matchday_kickoff_local),
            kickoffNow = strings.get(Res.string.matchday_kickoff_now),
            versus = strings.get(Res.string.fixture_versus),
            days = strings.get(Res.string.matchday_days),
            hours = strings.get(Res.string.matchday_hours),
            minutes = strings.get(Res.string.matchday_minutes),
            seconds = strings.get(Res.string.matchday_seconds),
            recent = strings.get(Res.string.matchday_recent),
            following = strings.get(Res.string.matchday_following),
            stadium = strings.get(Res.string.matchday_stadium),
            arena = strings.get(Res.string.matchday_arena),
            capacity = strings.get(Res.string.matchday_capacity),
            founded = strings.get(Res.string.matchday_founded),
        )
    }
}
