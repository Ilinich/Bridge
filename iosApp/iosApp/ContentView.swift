import SwiftUI
import Shared

struct ContentView: View {

    @StateObject private var navigation = AppNavigation()

    var body: some View {
        TabView(selection: $navigation.tab) {
            stack(.matchday) { MatchdayScreen() }
                .tabItem { Label("Matchday", systemImage: "shield") }
                .tag(Tab.matchday)
            stack(.season) { SeasonScreen() }
                .tabItem { Label("Season", systemImage: "calendar") }
                .tag(Tab.season)
            stack(.squad) { SquadScreen() }
                .tabItem { Label("Squad", systemImage: "person.2") }
                .tag(Tab.squad)
            stack(.club) { ClubScreen() }
                .tabItem { Label("Club", systemImage: "flag") }
                .tag(Tab.club)
        }
        .preferredColorScheme(.dark)
        .tint(.blue)
    }

    /// Each tab keeps its own history, which is what makes leaving a player open and coming back
    /// return to that player rather than to the top of the section.
    private func stack<Content: View>(_ tab: Tab, @ViewBuilder content: () -> Content) -> some View {
        NavigationStack(path: navigation.path(for: tab)) {
            content()
                .navigationDestination(for: Push.self) { screen in
                    switch screen {
                    case .player(let id): PlayerScreen(playerId: id)
                    case .matchDetail(let id): MatchDetailScreen(matchId: id)
                    }
                }
        }
    }
}
