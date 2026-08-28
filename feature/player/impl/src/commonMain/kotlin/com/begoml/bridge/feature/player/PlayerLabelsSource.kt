package com.begoml.bridge.feature.player

import bridge.feature.player.impl.generated.resources.Res
import bridge.feature.player.impl.generated.resources.player_back
import bridge.feature.player.impl.generated.resources.player_country
import bridge.feature.player.impl.generated.resources.player_height
import bridge.feature.player.impl.generated.resources.player_not_found
import bridge.feature.player.impl.generated.resources.player_number
import bridge.feature.player.impl.generated.resources.player_position
import bridge.feature.player.impl.generated.resources.player_title
import com.begoml.bridge.foundation.strings.LabelsLoader
import com.begoml.bridge.foundation.strings.StringResolver

/**
 * This feature's fixed words, read once and held for the run.
 *
 * The ids are the feature's own; how they are turned into words is not its business, which is why
 * a resolver is injected rather than a reader imported.
 */
class PlayerLabelsSource(private val strings: StringResolver) : LabelsLoader {

    lateinit var labels: PlayerLabels
        private set

    override suspend fun load() {
        labels = PlayerLabels(
            title = strings.get(Res.string.player_title),
            back = strings.get(Res.string.player_back),
            notFound = strings.get(Res.string.player_not_found),
            number = strings.get(Res.string.player_number),
            position = strings.get(Res.string.player_position),
            country = strings.get(Res.string.player_country),
            height = strings.get(Res.string.player_height),
        )
    }
}
