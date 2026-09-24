package com.begoml.bridge

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
        backdropUrl = null,
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
            homeBadgeUrl = null,
            awayName = "Arsenal",
            awayCode = "ARS",
            awayBadgeUrl = null,
        ),
        recent = RecentMatchUi(
            teams = StringDesc.Raw("Chelsea — Hull City"),
            competition = "English Premier League",
            score = StringDesc.Raw("2 : 2"),
            awayBadgeUrl = null,
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
            card(id = "10", name = "Cole Palmer", position = "Attacking Midfield", number = "10", followed = true),
            card(id = "1", name = "Robert Sánchez", position = "Goalkeeper", number = "1"),
            card(id = "24", name = "Reece James", position = "Right-Back", number = "24"),
            card(id = "8", name = "Enzo Fernández", position = "Central Midfield", number = "8"),
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
                cutoutUrl = null,
                followed = true,
            ),
            PlayerPageUi(
                id = "24",
                name = "Reece James",
                shirtNumber = "24",
                position = "Right-Back",
                nationality = "England",
                height = "182cm / 6'0\"",
                cutoutUrl = null,
                followed = false,
            ),
        ),
    )

    fun club(): ClubUiState = ClubUiState(
        isLoading = false,
        club = ClubUi(
            name = "Chelsea",
            code = "CHE",
            badgeUrl = null,
            backdropUrl = null,
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
        id: String,
        name: String,
        position: String,
        number: String,
        followed: Boolean = false,
    ): PlayerCardUi = PlayerCardUi(
        id = id,
        name = name,
        position = position,
        shirtNumber = number,
        cutoutUrl = null,
        followed = followed,
    )

    private const val KickoffMillis = 1_760_112_000_000L
}
