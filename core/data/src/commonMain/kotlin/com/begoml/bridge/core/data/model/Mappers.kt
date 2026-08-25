package com.begoml.bridge.core.data.model

import com.begoml.bridge.core.data.TeamNames
import com.begoml.bridge.core.data.openfootball.SeasonEnvelope
import com.begoml.bridge.core.data.sportsdb.EventDto
import com.begoml.bridge.core.data.sportsdb.PlayerDto
import com.begoml.bridge.core.data.sportsdb.TeamDto
import com.begoml.bridge.core.data.sportsdb.VenueDto
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.time.Instant

private const val MinutesInDefaultKickoff = 0

internal fun TeamDto.toClub(): Club? {
    val id = id ?: return null
    val name = name ?: return null
    return Club(
        id = id,
        name = TeamNames.displayName(name),
        code = shortName?.takeIf { it.isNotBlank() } ?: TeamNames.code(name),
        foundedYear = formedYear?.toIntOrNull(),
        stadium = stadium?.takeIf { it.isNotBlank() },
        stadiumCapacity = stadiumCapacity?.toIntOrNull(),
        location = location?.takeIf { it.isNotBlank() },
        description = description?.takeIf { it.isNotBlank() },
        media = ClubMedia(
            badgeUrl = badge?.takeIf { it.isNotBlank() },
            logoUrl = logo?.takeIf { it.isNotBlank() },
            bannerUrl = banner?.takeIf { it.isNotBlank() },
            fanartUrls = listOfNotNull(fanart1, fanart2, fanart3, fanart4).filter { it.isNotBlank() },
        ),
        details = ClubDetails(
            nicknames = keywords.orEmpty().split(',').map { it.trim() }.filter { it.isNotEmpty() },
            colours = ClubColours(
                primary = colour1?.takeIf { it.isNotBlank() },
                secondary = colour2?.takeIf { it.isNotBlank() },
                tertiary = colour3?.takeIf { it.isNotBlank() },
            ),
            links = ClubLinks(
                website = website?.takeIf { it.isNotBlank() },
                youtube = youtube?.takeIf { it.isNotBlank() },
                twitter = twitter?.takeIf { it.isNotBlank() },
                instagram = instagram?.takeIf { it.isNotBlank() },
            ),
            venueId = venueId?.takeIf { it.isNotBlank() },
        ),
    )
}

internal fun VenueDto.toVenue(): Venue? {
    val id = id ?: return null
    val name = name ?: return null
    return Venue(
        id = id,
        name = name,
        description = description?.takeIf { it.isNotBlank() },
        capacity = capacity?.toIntOrNull(),
        openedYear = openedYear?.toIntOrNull(),
        location = location?.takeIf { it.isNotBlank() },
        thumbUrl = thumb?.takeIf { it.isNotBlank() },
        fanartUrl = fanart1?.takeIf { it.isNotBlank() },
        map = map?.takeIf { it.isNotBlank() },
        website = website?.takeIf { it.isNotBlank() },
    )
}

internal fun EventDto.toMatch(): Match? {
    val id = id ?: return null
    val home = homeTeam ?: return null
    val away = awayTeam ?: return null
    val kickoff = parseKickoff() ?: return null

    return Match(
        id = id,
        competition = league.orEmpty(),
        home = teamRef(home, homeBadge),
        away = teamRef(away, awayBadge),
        kickoff = kickoff,
        venue = venue?.takeIf { it.isNotBlank() },
        status = status.toMatchStatus(),
        score = score(homeScore, awayScore),
    )
}

internal fun PlayerDto.toPlayer(): Player? {
    val id = id ?: return null
    val name = name ?: return null
    return Player(
        id = id,
        name = name,
        position = position?.takeIf { it.isNotBlank() },
        shirtNumber = number?.takeIf { it.isNotBlank() },
        nationality = nationality?.takeIf { it.isNotBlank() },
        height = height?.takeIf { it.isNotBlank() },
        description = description?.takeIf { it.isNotBlank() },
        cutoutUrl = cutout?.takeIf { it.isNotBlank() },
        thumbUrl = thumb?.takeIf { it.isNotBlank() },
    )
}

internal fun SeasonEnvelope.toSeason(seasonId: String): Season {
    val matches = matches.mapNotNull { dto ->
        val round = dto.round?.roundNumber() ?: return@mapNotNull null
        val home = dto.homeTeam ?: return@mapNotNull null
        val away = dto.awayTeam ?: return@mapNotNull null
        val kickoff = parseDate(dto.date, dto.time) ?: return@mapNotNull null

        SeasonMatch(
            id = seasonMatchId(seasonId, round, home, away),
            round = round,
            kickoff = kickoff,
            home = teamRef(home, badgeUrl = null),
            away = teamRef(away, badgeUrl = null),
            score = dto.score?.let { MatchScore(home = it[0], away = it[1]) },
        )
    }

    val rounds = matches
        .groupBy { it.round }
        .map { (number, roundMatches) ->
            SeasonRound(number = number, matches = roundMatches.sortedBy { it.kickoff })
        }
        .sortedBy { it.number }

    return Season(name = name.orEmpty(), rounds = rounds)
}

/** The feed has no fixture id, so one is derived from the only fields that identify it. */
internal fun seasonMatchId(seasonId: String, round: Int, home: String, away: String): String =
    "$seasonId-$round-${TeamNames.code(home)}-${TeamNames.code(away)}"

private fun teamRef(name: String, badgeUrl: String?) = TeamRef(
    name = TeamNames.displayName(name),
    code = TeamNames.code(name),
    badgeUrl = badgeUrl?.takeIf { it.isNotBlank() },
)

private fun score(home: String?, away: String?): MatchScore? {
    val homeGoals = home?.toIntOrNull() ?: return null
    val awayGoals = away?.toIntOrNull() ?: return null
    return MatchScore(home = homeGoals, away = awayGoals)
}

/** `"Matchday 12"` sorts before `"Matchday 2"` as text, so only the number survives mapping. */
internal fun String.roundNumber(): Int? = trim().substringAfterLast(' ').toIntOrNull()

private fun EventDto.parseKickoff(): Instant? {
    timestamp?.let { raw -> runCatching { LocalDateTime.parse(raw) }.getOrNull() }
        ?.let { return it.toInstant(TimeZone.UTC) }
    return parseDate(date, time)
}

private fun parseDate(date: String?, time: String?): Instant? {
    val day = date?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return null
    val clock = time
        ?.let { raw -> runCatching { LocalTime.parse(raw.padTime()) }.getOrNull() }
        ?: LocalTime(hour = 12, minute = MinutesInDefaultKickoff)
    return LocalDateTime(day, clock).toInstant(TimeZone.UTC)
}

private fun String.padTime(): String = if (count { it == ':' } == 1) "$this:00" else this

private fun String?.toMatchStatus(): MatchStatus = when (this?.uppercase()) {
    null, "", "NS" -> MatchStatus.SCHEDULED
    "FT", "AET", "PEN" -> MatchStatus.FINISHED
    else -> MatchStatus.IN_PLAY
}
