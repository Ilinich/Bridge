package com.begoml.bridge.feature.club

import bridge.feature.club.impl.generated.resources.Res
import bridge.feature.club.impl.generated.resources.club_about
import bridge.feature.club.impl.generated.resources.club_capacity
import bridge.feature.club.impl.generated.resources.club_colours
import bridge.feature.club.impl.generated.resources.club_founded
import bridge.feature.club.impl.generated.resources.club_ground
import bridge.feature.club.impl.generated.resources.club_instagram
import bridge.feature.club.impl.generated.resources.club_links
import bridge.feature.club.impl.generated.resources.club_location
import bridge.feature.club.impl.generated.resources.club_media
import bridge.feature.club.impl.generated.resources.club_opened
import bridge.feature.club.impl.generated.resources.club_twitter
import bridge.feature.club.impl.generated.resources.club_website
import bridge.feature.club.impl.generated.resources.club_youtube
import com.begoml.bridge.foundation.strings.LabelsLoader
import com.begoml.bridge.foundation.strings.StringResolver

/**
 * This feature's fixed words, read once and held for the run.
 *
 * The ids are the feature's own; how they are turned into words is not its business, which is why
 * a resolver is injected rather than a reader imported.
 */
class ClubLabelsSource(private val strings: StringResolver) : LabelsLoader {

    lateinit var labels: ClubLabels
        private set

    override suspend fun load() {
        labels = ClubLabels(
            about = strings.get(Res.string.club_about),
            media = strings.get(Res.string.club_media),
            ground = strings.get(Res.string.club_ground),
            links = strings.get(Res.string.club_links),
            founded = strings.get(Res.string.club_founded),
            colours = strings.get(Res.string.club_colours),
            capacity = strings.get(Res.string.club_capacity),
            opened = strings.get(Res.string.club_opened),
            location = strings.get(Res.string.club_location),
            website = strings.get(Res.string.club_website),
            youtube = strings.get(Res.string.club_youtube),
            twitter = strings.get(Res.string.club_twitter),
            instagram = strings.get(Res.string.club_instagram),
        )
    }
}
