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

    var body: some View {
        // One clock for the whole grid: the Compose cards share a single compiled program and a
        // single animation, and a card per timeline would be N animations for one effect.
        TimelineView(.animation) { context in
            let time = context.date.timeIntervalSinceReferenceDate
            Screen {
                LazyVGrid(columns: columns, spacing: 9) {
                    ForEach(model.state.players, id: \.id) { player in
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
    }
}
