package com.begoml.bridge.core.data.db

import com.begoml.bridge.core.data.model.Club
import com.begoml.bridge.core.data.model.MatchScore
import com.begoml.bridge.core.data.model.Player
import com.begoml.bridge.core.data.model.Season
import com.begoml.bridge.core.data.model.SeasonMatch
import com.begoml.bridge.core.data.model.SeasonRound
import com.begoml.bridge.core.data.model.TeamRef
import kotlin.time.Instant

private const val FanartSeparator = "\n"

internal fun Club.toEntity() = ClubEntity(
    id = id,
    name = name,
    code = code,
    foundedYear = foundedYear,
    stadium = stadium,
    stadiumCapacity = stadiumCapacity,
    location = location,
    description = description,
    badgeUrl = badgeUrl,
    fanartUrls = fanartUrls.joinToString(FanartSeparator),
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
    badgeUrl = badgeUrl,
    fanartUrls = fanartUrls.split(FanartSeparator).filter { it.isNotBlank() },
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

private fun SeasonMatchEntity.toSeasonMatch() = SeasonMatch(
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
