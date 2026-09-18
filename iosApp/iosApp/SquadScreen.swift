import SwiftUI
import Shared

struct SquadScreen: View {

    @StateObject private var model: ScreenModel<ImplSquadUiState>
    private let component: ImplSquadComponent

    private let columns = [GridItem(.adaptive(minimum: 150), spacing: 14)]

    init() {
        let component = IosBridge.shared.squad()
        self.component = component
        _model = StateObject(
            wrappedValue: ScreenModel(flow: component.state, close: component.close)
        )
    }

    var body: some View {
        ScrollView {
            LazyVGrid(columns: columns, spacing: 14) {
                ForEach(model.state.players, id: \.id) { player in
                    Button {
                        component.viewModel.onPlayerClick(playerId: player.id)
                    } label: {
                        VStack(spacing: 8) {
                            Badge(url: player.cutoutUrl, code: player.shirtNumber ?? "", size: 72)
                            Text(player.name).font(.subheadline).foregroundStyle(.white)
                            if let position = player.position {
                                Text(position).font(.caption2).foregroundStyle(.secondary)
                            }
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 14)
                        .background(Color.panel, in: RoundedRectangle(cornerRadius: 16))
                    }
                }
            }
            .padding(20)
        }
        .screenBackground()
        .navigationTitle("Squad")
    }
}
