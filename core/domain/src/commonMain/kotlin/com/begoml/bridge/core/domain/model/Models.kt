package com.begoml.bridge.core.domain.model

import kotlin.time.Instant

/** A club as the app knows it, whichever source the name arrived from. */
data class TeamRef(
    val name: String,
    val code: String,
    val badgeUrl: String?,
)

data class MatchScore(val home: Int, val away: Int)

enum class MatchStatus { SCHEDULED, IN_PLAY, FINISHED }

data class Match(
    val id: String,
    val competition: String,
    val home: TeamRef,
    val away: TeamRef,
    val kickoff: Instant,
    val venue: String?,
    val status: MatchStatus,
    val score: MatchScore?,
)

data class SeasonMatch(
    val id: String,
    val round: Int,
    val kickoff: Instant,
    val home: TeamRef,
    val away: TeamRef,
    val score: MatchScore?,
)

data class SeasonRound(
    val number: Int,
    val matches: List<SeasonMatch>,
)

data class Season(
    val name: String,
    val rounds: List<SeasonRound>,
)

data class Club(
    val id: String,
    val name: String,
    val code: String,
    val foundedYear: Int?,
    val stadium: String?,
    val stadiumCapacity: Int?,
    val location: String?,
    val description: String?,
    val media: ClubMedia,
    val details: ClubDetails,
)

data class Player(
    val id: String,
    val name: String,
    val position: String?,
    val shirtNumber: String?,
    val nationality: String?,
    val height: String?,
    val description: String?,
    val cutoutUrl: String?,
    val thumbUrl: String?,
)
