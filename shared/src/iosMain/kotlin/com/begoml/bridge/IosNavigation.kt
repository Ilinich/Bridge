package com.begoml.bridge

import com.begoml.bridge.feature.club.api.ClubRoute
import com.begoml.bridge.feature.matches.api.MatchDetailRoute
import com.begoml.bridge.feature.matches.api.MatchdayRoute
import com.begoml.bridge.feature.matches.api.SeasonRoute
import com.begoml.bridge.feature.player.api.PlayerDetailRoute
import com.begoml.bridge.feature.squad.api.SquadRoute
import com.begoml.bridge.navigation.Route
import com.begoml.bridge.navigation.router.NavigationCommand

/**
 * What the Swift host is asked to do, in terms it can switch on exhaustively.
 *
 * Routes stay the features' own vocabulary; this is the host's. The translation lives here, in
 * one function, for two reasons: a Swift view has no business importing four features' api
 * modules, and the export only carries types the exported API mentions — a route class nobody
 * names never reaches Swift at all.
 */
sealed interface IosNavigation {

    data object Up : IosNavigation

    data object Matchday : IosNavigation

    data object Season : IosNavigation

    data object Squad : IosNavigation

    data object Club : IosNavigation

    data class Player(val playerId: String) : IosNavigation

    data class MatchDetail(val matchId: String) : IosNavigation
}

/** Null for a destination this host has no screen for; the command is then ignored rather than obeyed badly. */
internal fun NavigationCommand.toIosNavigation(): IosNavigation? = when (this) {
    is NavigationCommand.NavigateUp -> IosNavigation.Up
    is NavigationCommand.NavigateTo -> destination.toIosNavigation()
}

private fun Route.toIosNavigation(): IosNavigation? = when (this) {
    is MatchdayRoute -> IosNavigation.Matchday
    is SeasonRoute -> IosNavigation.Season
    is SquadRoute -> IosNavigation.Squad
    is ClubRoute -> IosNavigation.Club
    is PlayerDetailRoute -> IosNavigation.Player(playerId)
    is MatchDetailRoute -> IosNavigation.MatchDetail(matchId)
    else -> null
}
