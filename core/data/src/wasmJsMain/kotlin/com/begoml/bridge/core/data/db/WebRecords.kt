package com.begoml.bridge.core.data.db

import com.begoml.bridge.core.domain.model.Club
import com.begoml.bridge.core.domain.model.ClubColours
import com.begoml.bridge.core.domain.model.ClubDetails
import com.begoml.bridge.core.domain.model.ClubLinks
import com.begoml.bridge.core.domain.model.ClubMedia
import com.begoml.bridge.core.domain.model.MatchScore
import com.begoml.bridge.core.domain.model.Player
import com.begoml.bridge.core.domain.model.SeasonMatch
import com.begoml.bridge.core.domain.model.TeamRef
import com.begoml.bridge.core.domain.model.Venue
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * What the browser keeps, which is not what the app reasons about.
 *
 * These exist for the same reason the Room entities do: a stored shape has to survive the next
 * version of the model, and a domain class carrying `@Serializable` for a storage decision is a
 * domain class that can no longer change freely. Times are epoch millis, because a stored
 * timestamp outlives whichever time library was fashionable when it was written.
 */
@Serializable
internal data class ClubRecord(
    val id: String,
    val name: String,
    val code: String,
    val foundedYear: Int? = null,
    val stadium: String? = null,
    val stadiumCapacity: Int? = null,
    val location: String? = null,
    val description: String? = null,
    val badgeUrl: String? = null,
    val logoUrl: String? = null,
    val bannerUrl: String? = null,
    val fanartUrls: List<String> = emptyList(),
    val nicknames: List<String> = emptyList(),
    val colourPrimary: String? = null,
    val colourSecondary: String? = null,
    val colourTertiary: String? = null,
    val website: String? = null,
    val youtube: String? = null,
    val twitter: String? = null,
    val instagram: String? = null,
    val venueId: String? = null,
)

@Serializable
internal data class VenueRecord(
    val id: String,
    val name: String,
    val description: String? = null,
    val capacity: Int? = null,
    val openedYear: Int? = null,
    val location: String? = null,
    val thumbUrl: String? = null,
    val fanartUrl: String? = null,
    val map: String? = null,
    val website: String? = null,
)

@Serializable
internal data class PlayerRecord(
    val id: String,
    val name: String,
    val position: String? = null,
    val shirtNumber: String? = null,
    val nationality: String? = null,
    val height: String? = null,
    val description: String? = null,
    val cutoutUrl: String? = null,
    val thumbUrl: String? = null,
)

@Serializable
internal data class SeasonMatchRecord(
    val id: String,
    val round: Int,
    val kickoffMillis: Long,
    val homeName: String,
    val homeCode: String,
    val awayName: String,
    val awayCode: String,
    val homeGoals: Int? = null,
    val awayGoals: Int? = null,
)

internal fun Club.toRecord() = ClubRecord(
    id = id,
    name = name,
    code = code,
    foundedYear = foundedYear,
    stadium = stadium,
    stadiumCapacity = stadiumCapacity,
    location = location,
    description = description,
    badgeUrl = media.badgeUrl,
    logoUrl = media.logoUrl,
    bannerUrl = media.bannerUrl,
    fanartUrls = media.fanartUrls,
    nicknames = details.nicknames,
    colourPrimary = details.colours.primary,
    colourSecondary = details.colours.secondary,
    colourTertiary = details.colours.tertiary,
    website = details.links.website,
    youtube = details.links.youtube,
    twitter = details.links.twitter,
    instagram = details.links.instagram,
    venueId = details.venueId,
)

internal fun ClubRecord.toClub() = Club(
    id = id,
    name = name,
    code = code,
    foundedYear = foundedYear,
    stadium = stadium,
    stadiumCapacity = stadiumCapacity,
    location = location,
    description = description,
    media = ClubMedia(
        badgeUrl = badgeUrl,
        logoUrl = logoUrl,
        bannerUrl = bannerUrl,
        fanartUrls = fanartUrls,
    ),
    details = ClubDetails(
        nicknames = nicknames,
        colours = ClubColours(colourPrimary, colourSecondary, colourTertiary),
        links = ClubLinks(website, youtube, twitter, instagram),
        venueId = venueId,
    ),
)

internal fun Venue.toRecord() = VenueRecord(
    id = id,
    name = name,
    description = description,
    capacity = capacity,
    openedYear = openedYear,
    location = location,
    thumbUrl = thumbUrl,
    fanartUrl = fanartUrl,
    map = map,
    website = website,
)

internal fun VenueRecord.toVenue() = Venue(
    id = id,
    name = name,
    description = description,
    capacity = capacity,
    openedYear = openedYear,
    location = location,
    thumbUrl = thumbUrl,
    fanartUrl = fanartUrl,
    map = map,
    website = website,
)

internal fun Player.toRecord() = PlayerRecord(
    id = id,
    name = name,
    position = position,
    shirtNumber = shirtNumber,
    nationality = nationality,
    height = height,
    description = description,
    cutoutUrl = cutoutUrl,
    thumbUrl = thumbUrl,
)

internal fun PlayerRecord.toPlayer() = Player(
    id = id,
    name = name,
    position = position,
    shirtNumber = shirtNumber,
    nationality = nationality,
    height = height,
    description = description,
    cutoutUrl = cutoutUrl,
    thumbUrl = thumbUrl,
)

internal fun SeasonMatch.toRecord() = SeasonMatchRecord(
    id = id,
    round = round,
    kickoffMillis = kickoff.toEpochMilliseconds(),
    homeName = home.name,
    homeCode = home.code,
    awayName = away.name,
    awayCode = away.code,
    homeGoals = score?.home,
    awayGoals = score?.away,
)

internal fun SeasonMatchRecord.toSeasonMatch() = SeasonMatch(
    id = id,
    round = round,
    kickoff = Instant.fromEpochMilliseconds(kickoffMillis),
    home = TeamRef(name = homeName, code = homeCode, badgeUrl = null),
    away = TeamRef(name = awayName, code = awayCode, badgeUrl = null),
    score = if (homeGoals != null && awayGoals != null) {
        MatchScore(home = homeGoals, away = awayGoals)
    } else {
        null
    },
)
