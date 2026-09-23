import Shared
import Testing

@testable import Bridge

/// The half of the navigation loop that lives in Swift: the shared code decides where to go, this
/// decides what that does to four stacks. On the Kotlin side the same decisions are covered by
/// AppRouterTest and IosNavigationTest.
@MainActor
struct AppNavigationTests {

    @Test func a_tab_root_selects_its_tab_rather_than_being_pushed() {
        let navigation = AppNavigation(observing: false)

        navigation.apply(IosNavigationClub())

        #expect(navigation.tab == .club)
        #expect(navigation.paths[.club, default: []].isEmpty)
    }

    @Test func a_destination_with_an_argument_is_pushed_onto_the_open_tab() {
        let navigation = AppNavigation(observing: false)
        navigation.apply(IosNavigationSquad())

        navigation.apply(IosNavigationPlayer(playerId: "34"))

        #expect(navigation.paths[.squad] == [.player("34")])
    }

    @Test func each_tab_keeps_its_own_history() {
        let navigation = AppNavigation(observing: false)
        navigation.apply(IosNavigationSquad())
        navigation.apply(IosNavigationPlayer(playerId: "34"))

        navigation.apply(IosNavigationSeason())
        navigation.apply(IosNavigationMatchDetail(matchId: "9001"))

        #expect(navigation.paths[.squad] == [.player("34")])
        #expect(navigation.paths[.season] == [.matchDetail("9001")])
    }

    @Test func up_pops_the_open_tab_and_stops_at_its_root() {
        let navigation = AppNavigation(observing: false)
        navigation.apply(IosNavigationSquad())
        navigation.apply(IosNavigationPlayer(playerId: "34"))

        navigation.apply(IosNavigationUp())
        navigation.apply(IosNavigationUp())

        #expect(navigation.paths[.squad, default: []].isEmpty)
        #expect(navigation.tab == .squad)
    }
}

struct LoadableStateTests {

    @Test func a_failure_is_reported_before_a_load() {
        #expect(LoadableState(isLoading: true, hasFailed: true) == .failed)
    }

    @Test func content_comes_only_when_there_is_content() {
        #expect(LoadableState(isLoading: true, hasFailed: false) == .loading)
        #expect(LoadableState(isLoading: false, hasFailed: false) == .content)
    }
}
