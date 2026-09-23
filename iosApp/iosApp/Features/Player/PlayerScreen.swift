import SwiftUI
import Shared

struct PlayerScreen: View {

    let playerId: String

    @StateObject private var model: ScreenModel<ImplPlayerComponent, ImplPlayerUiState>

    /// Which page is in view. Seeded with the player that was tapped rather than set on appear:
    /// set later, the pager has already settled on the first page and the title names someone the
    /// screen is not showing.
    @State private var shown: String?

    init(playerId: String) {
        self.playerId = playerId
        _shown = State(initialValue: playerId)
        _model = StateObject(
            wrappedValue: ScreenModel(
                component: IosBridge.shared.player(),
                state: { $0.state },
                close: { $0.close() }
            )
        )
    }

    var body: some View {
        let state = model.state
        let players = state.players

        ZStack(alignment: .bottom) {
            // The whole screen is the shader, as it is on the Compose side: the pages ride over it
            // and it never moves with them.
            ClubWash()

            GeometryReader { screen in
                ScrollViewReader { pager in
                    ScrollView(.horizontal) {
                        LazyHStack(spacing: 0) {
                            ForEach(players, id: \.id) { player in
                                PlayerPage(
                                    player: player,
                                    labels: state.labels,
                                    width: screen.size.width,
                                    onFollow: { model.component.viewModel.onFollowClick(playerId: $0) }
                                )
                                .frame(width: screen.size.width)
                                .id(player.id)
                            }
                        }
                        .scrollTargetLayout()
                    }
                    .scrollTargetBehavior(.paging)
                    .scrollIndicators(.hidden)
                    .scrollPosition(id: $shown, anchor: .center)
                    // scrollPosition reports where the pager is; it does not put it there on the
                    // first layout, and the squad opens on whichever player was tapped.
                    .task(id: players.count) {
                        guard !players.isEmpty else { return }
                        pager.scrollTo(playerId, anchor: .center)
                    }
                }
            }

            PageDots(count: players.count, selected: index(in: players))
                .padding(.bottom, 44)
        }
        .navigationTitle(current(in: players)?.name ?? state.labels.title.localized())
        .navigationBarTitleDisplayMode(.inline)
        .toolbarBackground(.hidden, for: .navigationBar)
    }

    private func current(in players: [ImplPlayerPageUi]) -> ImplPlayerPageUi? {
        players.first { $0.id == (shown ?? playerId) }
    }

    private func index(in players: [ImplPlayerPageUi]) -> Int {
        players.firstIndex { $0.id == (shown ?? playerId) } ?? 0
    }
}
