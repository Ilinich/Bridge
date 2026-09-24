import SwiftUI
import Shared

struct SquadScreen: View {

    @StateObject private var model = ScreenModel(
        component: IosBridge.shared.squad(),
        state: { $0.state },
        close: { $0.close() }
    )

    var body: some View {
        let actions = model.component.viewModel
        SquadContent(
            state: model.state,
            onPlayer: { actions.onPlayerClick(playerId: $0) },
            onRetry: { actions.retry() }
        )
    }
}

/// Everything this screen draws, and nothing about where the state came from — which is what lets
/// a preview draw it from `IosPreviews` without a graph behind it.
struct SquadContent: View {

    let state: ImplSquadUiState
    let onPlayer: (String) -> Void
    let onRetry: () -> Void

    @Environment(\.isOnScreen) private var isOnScreen

    private let columns = [
        GridItem(.flexible(), spacing: 9),
        GridItem(.flexible(), spacing: 9)
    ]

    var body: some View {
        Screen {
            // One clock for the whole grid: the Compose cards share a single compiled program and
            // a single animation, and a card per timeline would be N animations for one effect.
            // The clock runs only while this is the page in view — all four tabs stay alive, and
            // an animation in a tab nobody is looking at is a battery bill.
            Group {
                if isOnScreen {
                    TimelineView(.animation) { context in
                        grid(at: context.date.timeIntervalSinceReferenceDate)
                    }
                } else {
                    grid(at: 0)
                }
            }
            .loadable(
                isLoading: state.isLoading && state.players.isEmpty,
                hasFailed: state.error != nil && state.players.isEmpty,
                onRetry: onRetry
            )
        }
    }

    private func grid(at time: Double) -> some View {
        LazyVGrid(columns: columns, spacing: 9) {
            ForEach(state.players, id: \.id) { player in
                Button { onPlayer(player.id) } label: {
                    PlayerCard(player: player, time: time)
                }
                .buttonStyle(.plain)
            }
        }
    }
}

#Preview("Squad") {
    PreviewHost {
        SquadContent(state: IosPreviews.shared.squad(), onPlayer: { _ in }, onRetry: {})
    }
}

#Preview("Squad, first load") {
    PreviewHost {
        SquadContent(state: IosPreviews.shared.squadLoading(), onPlayer: { _ in }, onRetry: {})
    }
}

#Preview("Squad, nothing loaded") {
    PreviewHost {
        SquadContent(state: IosPreviews.shared.squadFailed(), onPlayer: { _ in }, onRetry: {})
    }
}
