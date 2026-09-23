import SwiftUI
import Shared

struct SquadScreen: View {

    @StateObject private var model = ScreenModel(
        component: IosBridge.shared.squad(),
        state: { $0.state },
        close: { $0.close() }
    )

    @Environment(\.isOnScreen) private var isOnScreen

    private let columns = [
        GridItem(.flexible(), spacing: 9),
        GridItem(.flexible(), spacing: 9)
    ]

    var body: some View {
        let state = model.state
        let actions = model.component.viewModel

        Screen {
            // One clock for the whole grid: the Compose cards share a single compiled program and
            // a single animation, and a card per timeline would be N animations for one effect.
            // The clock runs only while this is the page in view — all four tabs stay alive, and
            // an animation in a tab nobody is looking at is a battery bill.
            Group {
                if isOnScreen {
                    TimelineView(.animation) { context in
                        grid(state, at: context.date.timeIntervalSinceReferenceDate, actions: actions)
                    }
                } else {
                    grid(state, at: 0, actions: actions)
                }
            }
            .loadable(
                isLoading: state.isLoading && state.players.isEmpty,
                hasFailed: state.error != nil && state.players.isEmpty,
                onRetry: { actions.retry() }
            )
        }
    }

    private func grid(
        _ state: ImplSquadUiState,
        at time: Double,
        actions: ImplSquadViewModel
    ) -> some View {
        LazyVGrid(columns: columns, spacing: 9) {
            ForEach(state.players, id: \.id) { player in
                Button { actions.onPlayerClick(playerId: player.id) } label: {
                    PlayerCard(player: player, time: time)
                }
                .buttonStyle(.plain)
            }
        }
    }
}
