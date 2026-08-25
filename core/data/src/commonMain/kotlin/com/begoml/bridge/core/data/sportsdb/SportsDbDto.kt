package com.begoml.bridge.core.data.sportsdb

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Envelopes for TheSportsDB.
 *
 * `eventsnext` roots its list at `events` while `eventslast` roots the same shape at `results`,
 * so the two need separate wrappers even though the element type is identical.
 */
@Serializable
internal class TeamsEnvelope(@SerialName("teams") val teams: List<TeamDto>? = null)

@Serializable
internal class NextEventsEnvelope(@SerialName("events") val events: List<EventDto>? = null)

@Serializable
internal class LastEventsEnvelope(@SerialName("results") val results: List<EventDto>? = null)

@Serializable
internal class PlayersEnvelope(@SerialName("player") val players: List<PlayerDto>? = null)

/** Every field arrives as a string, including the numeric ones. */
@Serializable
internal class TeamDto(
    @SerialName("idTeam") val id: String? = null,
    @SerialName("strTeam") val name: String? = null,
    @SerialName("strTeamShort") val shortName: String? = null,
    @SerialName("intFormedYear") val formedYear: String? = null,
    @SerialName("strStadium") val stadium: String? = null,
    @SerialName("intStadiumCapacity") val stadiumCapacity: String? = null,
    @SerialName("strLocation") val location: String? = null,
    @SerialName("strDescriptionEN") val description: String? = null,
    @SerialName("strBadge") val badge: String? = null,
    @SerialName("strFanart1") val fanart1: String? = null,
    @SerialName("strFanart2") val fanart2: String? = null,
    @SerialName("strFanart3") val fanart3: String? = null,
    @SerialName("strFanart4") val fanart4: String? = null,
)

@Serializable
internal class EventDto(
    @SerialName("idEvent") val id: String? = null,
    @SerialName("strEvent") val name: String? = null,
    @SerialName("strLeague") val league: String? = null,
    @SerialName("strTimestamp") val timestamp: String? = null,
    @SerialName("dateEvent") val date: String? = null,
    @SerialName("strTime") val time: String? = null,
    @SerialName("intRound") val round: String? = null,
    @SerialName("strVenue") val venue: String? = null,
    @SerialName("strStatus") val status: String? = null,
    @SerialName("strHomeTeam") val homeTeam: String? = null,
    @SerialName("strAwayTeam") val awayTeam: String? = null,
    @SerialName("strHomeTeamBadge") val homeBadge: String? = null,
    @SerialName("strAwayTeamBadge") val awayBadge: String? = null,
    @SerialName("intHomeScore") val homeScore: String? = null,
    @SerialName("intAwayScore") val awayScore: String? = null,
)

@Serializable
internal class PlayerDto(
    @SerialName("idPlayer") val id: String? = null,
    @SerialName("strPlayer") val name: String? = null,
    @SerialName("strPosition") val position: String? = null,
    @SerialName("strNumber") val number: String? = null,
    @SerialName("strNationality") val nationality: String? = null,
    @SerialName("strHeight") val height: String? = null,
    @SerialName("strDescriptionEN") val description: String? = null,
    @SerialName("strCutout") val cutout: String? = null,
    @SerialName("strThumb") val thumb: String? = null,
)
