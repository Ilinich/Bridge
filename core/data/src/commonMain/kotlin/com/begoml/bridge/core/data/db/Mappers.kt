package com.begoml.bridge.core.data.db

import com.begoml.bridge.core.data.model.Club
import com.begoml.bridge.core.data.model.ClubColours
import com.begoml.bridge.core.data.model.ClubDetails
import com.begoml.bridge.core.data.model.ClubLinks
import com.begoml.bridge.core.data.model.ClubMedia
import com.begoml.bridge.core.data.model.MatchScore
import com.begoml.bridge.core.data.model.Player
import com.begoml.bridge.core.data.model.Season
import com.begoml.bridge.core.data.model.SeasonMatch
import com.begoml.bridge.core.data.model.SeasonRound
import com.begoml.bridge.core.data.model.TeamRef
import com.begoml.bridge.core.data.model.Venue
import kotlin.time.Instant

private const val ListSeparator = "\n"

internal fun Club.toEntity() = ClubEntity(
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
    fanartUrls = media.fanartUrls.joinToString(ListSeparator),
    nicknames = details.nicknames.joinToString(ListSeparator),
    colourPrimary = details.colours.primary,
    colourSecondary = details.colours.secondary,
    colourTertiary = details.colours.tertiary,
    website = details.links.website,
    youtube = details.links.youtube,
    twitter = details.links.twitter,
    instagram = details.links.instagram,
    venueId = details.venueId,
)

internal fun ClubEntity.toClub() = Club(
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
        fanartUrls = fanartUrls.split(ListSeparator).filter { it.isNotBlank() },
    ),
    details = ClubDetails(
        nicknames = nicknames.split(ListSeparator).filter { it.isNotBlank() },
        colours = ClubColours(colourPrimary, colourSecondary, colourTertiary),
        links = ClubLinks(website, youtube, twitter, instagram),
        venueId = venueId,
    ),
)

internal fun Venue.toEntity() = VenueEntity(
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

internal fun VenueEntity.toVenue() = Venue(
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

internal fun Player.toEntity(ordinal: Int) = PlayerEntity(
    id = id,
    name = name,
    position = position,
    shirtNumber = shirtNumber,
    nationality = nationality,
    height = height,
    description = description,
    cutoutUrl = cutoutUrl,
    thumbUrl = thumbUrl,
    ordinal = ordinal,
)

internal fun PlayerEntity.toPlayer() = Player(
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

internal fun SeasonMatch.toEntity(seasonId: String) = SeasonMatchEntity(
    id = id,
    season = seasonId,
    round = round,
    kickoffMillis = kickoff.toEpochMilliseconds(),
    homeName = home.name,
    homeCode = home.code,
    awayName = away.name,
    awayCode = away.code,
    homeGoals = score?.home,
    awayGoals = score?.away,
)

internal fun List<SeasonMatchEntity>.toSeason(name: String): Season = Season(
    name = name,
    rounds = groupBy { it.round }
        .map { (number, rows) ->
            SeasonRound(number = number, matches = rows.map { it.toSeasonMatch() })
        }
        .sortedBy { it.number },
)

internal fun SeasonMatchEntity.toSeasonMatch() = SeasonMatch(
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
