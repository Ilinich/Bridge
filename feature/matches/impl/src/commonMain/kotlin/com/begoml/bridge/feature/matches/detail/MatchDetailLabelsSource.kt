package com.begoml.bridge.feature.matches.detail

import bridge.feature.matches.impl.generated.resources.Res
import bridge.feature.matches.impl.generated.resources.match_away
import bridge.feature.matches.impl.generated.resources.match_back
import bridge.feature.matches.impl.generated.resources.match_draw
import bridge.feature.matches.impl.generated.resources.match_home
import bridge.feature.matches.impl.generated.resources.match_kickoff
import bridge.feature.matches.impl.generated.resources.match_loss
import bridge.feature.matches.impl.generated.resources.match_not_found
import bridge.feature.matches.impl.generated.resources.match_title
import bridge.feature.matches.impl.generated.resources.match_win
import com.begoml.bridge.foundation.strings.LabelsLoader
import com.begoml.bridge.foundation.strings.StringResolver

/**
 * This feature's fixed words, read once and held for the run.
 *
 * The ids are the feature's own; how they are turned into words is not its business, which is why
 * a resolver is injected rather than a reader imported.
 */
class MatchDetailLabelsSource(private val strings: StringResolver) : LabelsLoader {

    lateinit var labels: MatchDetailLabels
        private set

    override suspend fun load() {
        labels = MatchDetailLabels(
            title = strings.get(Res.string.match_title),
            back = strings.get(Res.string.match_back),
            notFound = strings.get(Res.string.match_not_found),
            kickoff = strings.get(Res.string.match_kickoff),
            homeLabel = strings.get(Res.string.match_home),
            awayLabel = strings.get(Res.string.match_away),
            win = strings.get(Res.string.match_win),
            draw = strings.get(Res.string.match_draw),
            loss = strings.get(Res.string.match_loss),
        )
    }
}
