import SwiftUI
import Shared

struct PlayerScreen: View {

    let playerId: String

    @StateObject private var model: ScreenModel<ImplPlayerComponent, ImplPlayerUiState>

    init(playerId: String) {
        self.playerId = playerId
        _model = StateObject(
            wrappedValue: ScreenModel(
                component: IosBridge.shared.player(),
                state: { $0.state },
                close: { $0.close() }
            )
        )
    }

    var body: some View {
        let actions = model.component.viewModel
        PlayerContent(
            state: model.state,
            opened: playerId,
            onFollow: { actions.onFollowClick(playerId: $0) }
        )
    }
}

/// Everything this screen draws, and nothing about where the state came from — which is what lets
/// a preview draw it from `IosPreviews` without a graph behind it.
struct PlayerContent: View {

    let state: ImplPlayerUiState
    let opened: String
    let onFollow: (String) -> Void

    /// Which page is in view. Seeded with the player that was tapped rather than set on appear:
    /// set later, the pager has already settled on the first page and the title names someone the
    /// screen is not showing.
    @State private var shown: String?

    init(state: ImplPlayerUiState, opened: String, onFollow: @escaping (String) -> Void) {
        self.state = state
        self.opened = opened
        self.onFollow = onFollow
        _shown = State(initialValue: opened)
    }

    var body: some View {
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
                                    onFollow: onFollow
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
                        pager.scrollTo(opened, anchor: .center)
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
        players.first { $0.id == (shown ?? opened) }
    }

    private func index(in players: [ImplPlayerPageUi]) -> Int {
        players.firstIndex { $0.id == (shown ?? opened) } ?? 0
    }
}

#Preview("Player") {
    PreviewHost {
        NavigationStack {
            PlayerContent(state: IosPreviews.shared.player(), opened: "10", onFollow: { _ in })
        }
    }
}
