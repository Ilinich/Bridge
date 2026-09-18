import SwiftUI
import Shared

struct ContentView: View {

    @StateObject private var navigation = AppNavigation()

    var body: some View {
        ZStack(alignment: .bottom) {
            TabView(selection: $navigation.tab) {
                stack(.matchday) { MatchdayScreen() }.tag(Tab.matchday)
                stack(.season) { SeasonScreen() }.tag(Tab.season)
                stack(.squad) { SquadScreen() }.tag(Tab.squad)
                stack(.club) { ClubScreen() }.tag(Tab.club)
            }
            .toolbar(.hidden, for: .tabBar)

            TabBar(selection: $navigation.tab)
        }
        .background(Color.ground)
        .preferredColorScheme(.dark)
        .tint(.clubBright)
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
