import SwiftUI
import Shared

struct SquadScreen: View {


    private let columns = [GridItem(.adaptive(minimum: 150), spacing: 14)]

    @StateObject private var model = ScreenModel(
        component: IosBridge.shared.squad(),
        state: { $0.state },
        close: { $0.close() }
    )

    var body: some View {
        Screen(backdropUrl: nil) {
            LazyVGrid(columns: columns, spacing: 14) {
                ForEach(model.state.players, id: \.id) { player in
                    Button {
                        model.component.viewModel.onPlayerClick(playerId: player.id)
                    } label: {
                        VStack(spacing: 8) {
                            Badge(url: player.cutoutUrl, code: player.shirtNumber ?? "", size: 84)
                            Text(player.name).font(.labelLarge).foregroundStyle(Color.textPrimary).lineLimit(1)
                            if let position = player.position {
                                Text(position).labelStyle()
                            }
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 12)
                        .background(Color.surface, in: RoundedRectangle(cornerRadius: 12))
                    }
                }
            }
        }
    }
}
