import SwiftUI
import Shared

struct RootView: View {

    @StateObject private var navigation = AppNavigation()

    var body: some View {
        ZStack {
            // All four pages stay alive, as they do in the Compose pager: switching tabs must not
            // reset where the user was. A TabView would do that too, but it also reserves a strip
            // for the bar it is told to hide, and that strip is a dead band under every screen.
            page(.matchday) { MatchdayScreen() }
            page(.season) { SeasonScreen() }
            page(.squad) { SquadScreen() }
            page(.club) { ClubScreen() }
        }
        .environmentObject(navigation)
        .background(Color.ground)
        .preferredColorScheme(.dark)
        .tint(.clubBright)
    }

    /// Each tab keeps its own history, which is what makes leaving a player open and coming back
    /// return to that player rather than to the top of the section.
    private func page<Content: View>(_ tab: Tab, @ViewBuilder content: () -> Content) -> some View {
        NavigationStack(path: navigation.path(for: tab)) {
            content()
                .navigationDestination(for: Push.self) { screen in
                    switch screen {
                    case .player(let id): PlayerScreen(playerId: id)
                    case .matchDetail(let id): MatchDetailScreen(matchId: id)
                    }
                }
        }
        .opacity(navigation.tab == tab ? 1 : 0)
        .allowsHitTesting(navigation.tab == tab)
        .zIndex(navigation.tab == tab ? 1 : 0)
        .environment(\.isOnScreen, navigation.tab == tab)
    }
}
