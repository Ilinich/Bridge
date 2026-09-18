package com.begoml.bridge

import com.begoml.bridge.feature.club.api.ClubRoute
import com.begoml.bridge.feature.matches.api.MatchDetailRoute
import com.begoml.bridge.feature.matches.api.MatchdayRoute
import com.begoml.bridge.feature.player.api.PlayerDetailRoute
import com.begoml.bridge.navigation.Route
import com.begoml.bridge.navigation.router.NavigationCommand
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private data object Unknown : Route {
    override val key: String = "unknown"
}

class IosNavigationTest {

    @Test
    fun a_tab_root_arrives_as_that_tab() {
        assertEquals(
            IosNavigation.Matchday,
            NavigationCommand.NavigateTo(MatchdayRoute).toIosNavigation(),
        )
        assertEquals(
            IosNavigation.Club,
            NavigationCommand.NavigateTo(ClubRoute).toIosNavigation(),
        )
    }

    @Test
    fun a_destination_with_an_argument_carries_it() {
        assertEquals(
            IosNavigation.Player(playerId = "34"),
            NavigationCommand.NavigateTo(PlayerDetailRoute("34")).toIosNavigation(),
        )
        assertEquals(
            IosNavigation.MatchDetail(matchId = "9001"),
            NavigationCommand.NavigateTo(MatchDetailRoute("9001")).toIosNavigation(),
        )
    }

    @Test
    fun up_needs_no_destination() {
        assertEquals(IosNavigation.Up, NavigationCommand.NavigateUp.toIosNavigation())
    }

    /**
     * A feature can own a destination this host has no screen for — the Compose host has scenes
     * that are not tabs here. Ignoring it is the right failure: the alternative is obeying it
     * badly, which lands the user on the wrong screen.
     */
    @Test
    fun a_destination_this_host_cannot_draw_is_ignored() {
        assertNull(NavigationCommand.NavigateTo(Unknown).toIosNavigation())
    }
}
