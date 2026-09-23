import SwiftUI
import Shared

struct SquadScreen: View {

    @StateObject private var model = ScreenModel(
        component: IosBridge.shared.squad(),
        state: { $0.state },
        close: { $0.close() }
    )

    private let columns = [
        GridItem(.flexible(), spacing: 9),
        GridItem(.flexible(), spacing: 9),
    ]

    @Environment(\.isOnScreen) private var isOnScreen

    var body: some View {
        let state = model.state
        Screen {
            LoadableView(
                isLoading: state.isLoading && state.players.isEmpty,
                hasFailed: state.error != nil && state.players.isEmpty,
                onRetry: { model.component.viewModel.retry() }
            ) {
                // One clock for the whole grid: the Compose cards share a single compiled program
                // and a single animation, and a card per timeline would be N animations for one
                // effect. The clock runs only while this is the page in view — all four tabs stay
                // alive, and an animation in a tab nobody is looking at is a battery bill.
                if isOnScreen {
                    TimelineView(.animation) { context in
                        grid(state: state, time: context.date.timeIntervalSinceReferenceDate)
                    }
                } else {
                    grid(state: state, time: 0)
                }
            }
        }
    }

    private func grid(state: ImplSquadUiState, time: Double) -> some View {
        LazyVGrid(columns: columns, spacing: 9) {
            ForEach(state.players, id: \.id) { player in
                Button {
                    model.component.viewModel.onPlayerClick(playerId: player.id)
                } label: {
                    PlayerCard(player: player, time: time)
                }
                .buttonStyle(.plain)
            }
        }
    }

}
