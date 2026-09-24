package com.begoml.bridge

import com.begoml.bridge.feature.club.ClubClip
import com.begoml.bridge.feature.club.ClubLinkUi
import com.begoml.bridge.feature.club.ClubUi
import com.begoml.bridge.feature.club.ClubUiState
import com.begoml.bridge.feature.club.GroundUi
import com.begoml.bridge.feature.matches.detail.MatchDetailUi
import com.begoml.bridge.feature.matches.detail.MatchDetailUiState
import com.begoml.bridge.feature.matches.matchday.FollowedPlayerUi
import com.begoml.bridge.feature.matches.matchday.MatchdayUiState
import com.begoml.bridge.feature.matches.matchday.NextMatchUi
import com.begoml.bridge.feature.matches.matchday.RecentMatchUi
import com.begoml.bridge.feature.matches.matchday.StadiumUi
import com.begoml.bridge.feature.matches.season.FixtureRowUi
import com.begoml.bridge.feature.matches.season.SeasonRoundUi
import com.begoml.bridge.feature.matches.season.SeasonUiState
import com.begoml.bridge.feature.player.PlayerPageUi
import com.begoml.bridge.feature.player.PlayerUiState
import com.begoml.bridge.feature.squad.PlayerCardUi
import com.begoml.bridge.feature.squad.SquadUiState
import dev.icerock.moko.resources.desc.Raw
import dev.icerock.moko.resources.desc.StringDesc
import kotlinx.collections.immutable.persistentListOf

/**
 * States for a SwiftUI preview to draw.
 *
 * They are built here rather than in Swift because these types are Kotlin's: a data class reaches
 * Swift with every parameter required and no defaults, and its lists are immutable collections a
 * Swift array cannot stand in for. Written over there, a mock would be a page of ceremony per
 * screen and would stop compiling the moment a field was added.
 *
 * The words are [StringDesc.Raw] wherever the real screen would resolve a resource: a preview is a
 * drawing of a layout, and a mock that goes looking for a bundle fails in the one process that has
 * the least reason to have one.
 */
@Suppress("TooManyFunctions")
object IosPreviews {

    fun matchday(): MatchdayUiState = MatchdayUiState(
        backdropUrl = Fanart,
        hasClub = true,
        isLoading = false,
        nextMatchLoaded = true,
        nextMatch = NextMatchUi(
            competition = "English Premier League",
            venue = "Stamford Bridge",
            kickoffText = "Oct 10 · 16:00",
            kickoffMillis = KickoffMillis,
            homeName = "Chelsea",
            homeCode = "CHE",
            homeBadgeUrl = ChelseaBadge,
            awayName = "Arsenal",
            awayCode = "ARS",
            awayBadgeUrl = ArsenalBadge,
        ),
        recent = RecentMatchUi(
            teams = StringDesc.Raw("Chelsea — Hull City"),
            competition = "English Premier League",
            score = StringDesc.Raw("2 : 2"),
            awayBadgeUrl = HullBadge,
            awayCode = "HUL",
        ),
        stadium = StadiumUi(arena = "Stamford Bridge", capacity = "40,343", founded = "1905"),
        following = persistentListOf(
            FollowedPlayerUi(id = "10", name = "Cole Palmer"),
            FollowedPlayerUi(id = "1", name = "Robert Sánchez"),
        ),
    )

    fun matchdayEmpty(): MatchdayUiState = MatchdayUiState(
        hasClub = true,
        isLoading = false,
        nextMatchLoaded = true,
        isOffline = true,
    )

    fun season(): SeasonUiState = SeasonUiState(
        isLoading = false,
        rounds = persistentListOf(
            SeasonRoundUi(
                number = 7,
                title = StringDesc.Raw("Matchweek 7"),
                matches = persistentListOf(
                    fixture(id = "1", teams = "Chelsea — Liverpool", day = "Oct 4", trailing = "2 : 1"),
                    fixture(id = "2", teams = "Brighton — Chelsea", day = "Sep 27", trailing = "1 : 1"),
                ),
            ),
            SeasonRoundUi(
                number = 8,
                title = StringDesc.Raw("Matchweek 8"),
                matches = persistentListOf(
                    fixture(id = "3", teams = "Chelsea — Arsenal", day = "Oct 10", trailing = "16:00", scored = false),
                ),
            ),
        ),
    )

    fun squad(): SquadUiState = SquadUiState(
        isLoading = false,
        players = persistentListOf(
            card("10", "Cole Palmer", "Attacking Midfield", "$Cutout/q6nnho1787689956.png", followed = true),
            card("30", "Aarón Anselmino", "Centre-Back", "$Cutout/g74uos1787690344.png"),
            card("18", "Danny Welbeck", "Centre-Forward", "$Cutout/mjo6g31787690678.png"),
            card("41", "Estêvão", "Right Winger", "$Cutout/gfgv301787727867.png"),
        ),
    )

    fun squadLoading(): SquadUiState = SquadUiState()

    fun squadFailed(): SquadUiState = SquadUiState(
        isLoading = false,
        error = IllegalStateException("preview"),
    )

    fun player(): PlayerUiState = PlayerUiState(
        isLoading = false,
        players = persistentListOf(
            PlayerPageUi(
                id = "10",
                name = "Cole Palmer",
                shirtNumber = "10",
                position = "Attacking Midfield",
                nationality = "England",
                height = "189cm / 6'2\"",
                cutoutUrl = "$Cutout/q6nnho1787689956.png",
                followed = true,
            ),
            PlayerPageUi(
                id = "18",
                name = "Danny Welbeck",
                shirtNumber = "18",
                position = "Centre-Forward",
                nationality = "England",
                height = "185cm / 6'1\"",
                cutoutUrl = "$Cutout/mjo6g31787690678.png",
                followed = false,
            ),
        ),
    )

    fun club(): ClubUiState = ClubUiState(
        isLoading = false,
        club = ClubUi(
            name = "Chelsea",
            code = "CHE",
            badgeUrl = ChelseaBadge,
            backdropUrl = Fanart,
            nicknames = "The Blues · The Pensioners",
            founded = "1905",
            summary = "Chelsea Football Club are a professional football club based in Fulham, London.",
            colours = persistentListOf("#034694", "#FFFFFF", "#DBA111"),
            links = persistentListOf(
                ClubLinkUi(label = StringDesc.Raw("Website"), url = "https://www.chelseafc.com"),
                ClubLinkUi(label = StringDesc.Raw("YouTube"), url = "https://www.youtube.com/chelseafc"),
            ),
        ),
        ground = GroundUi(
            name = "Stamford Bridge",
            thumbUrl = null,
            capacity = "40,343",
            opened = "1877",
            location = "Fulham, London",
            summary = null,
        ),
    )

    /** The clip the real screen plays, so the media section is not an empty black box. */
    fun clipUrl(): String = ClubClip.Url

    fun matchDetail(): MatchDetailUiState = MatchDetailUiState(
        isLoading = false,
        match = MatchDetailUi(
            homeName = "Chelsea",
            homeCode = "CHE",
            awayName = "Arsenal",
            awayCode = "ARS",
            scoreline = StringDesc.Raw("2 : 1"),
            kickoff = "Oct 10 · 16:00",
            round = StringDesc.Raw("Matchweek 8"),
            side = null,
            outcome = null,
        ),
    )

    private fun fixture(
        id: String,
        teams: String,
        day: String,
        trailing: String,
        scored: Boolean = true,
    ): FixtureRowUi = FixtureRowUi(
        id = id,
        homeCode = "CHE",
        teams = StringDesc.Raw(teams),
        day = day,
        trailing = StringDesc.Raw(trailing),
        hasScore = scored,
        highlighted = true,
    )

    private fun card(
        number: String,
        name: String,
        position: String,
        cutoutUrl: String,
        followed: Boolean = false,
    ): PlayerCardUi = PlayerCardUi(
        id = number,
        name = name,
        position = position,
        shirtNumber = number,
        cutoutUrl = cutoutUrl,
        followed = followed,
    )

    private const val KickoffMillis = 1_760_112_000_000L

    /**
     * The same CDN the app reads from, so a preview draws the pictures a screen really carries.
     * A canvas with no network falls back to the placeholders, which is a state worth seeing too.
     */
    private const val Media = "https://r2.thesportsdb.com/images/media"
    private const val Cutout = "$Media/player/cutout"
    private const val ChelseaBadge = "$Media/team/badge/pbf4ul1782638263.png"
    private const val ArsenalBadge = "$Media/team/badge/uyhbfe1612467038.png"
    private const val HullBadge = "$Media/team/badge/fbqqda1601726113.png"
    private const val Fanart = "$Media/team/fanart/v2gwen1731827710.jpg"
}
