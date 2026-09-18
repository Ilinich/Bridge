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
        let state = model.state
        let labels = state.labels
        let players = state.players
        let selected = players.firstIndex { $0.id == playerId } ?? 0

        TabView(selection: .constant(selected)) {
            ForEach(Array(players.enumerated()), id: \.element.id) { index, player in
                ScrollView {
                    VStack(spacing: 16) {
                        Badge(url: player.cutoutUrl, code: player.shirtNumber ?? "", size: 180)
                        Text(player.name).font(.headline).foregroundStyle(Color.textPrimary)
                        SurfaceRow(spacing: 6) {
                            if let number = player.shirtNumber {
                                Fact(label: labels.number.localized(), value: number)
                                    .frame(maxWidth: .infinity, alignment: .leading)
                            }
                            if let position = player.position {
                                Fact(label: labels.position.localized(), value: position)
                                    .frame(maxWidth: .infinity, alignment: .leading)
                            }
                            if let nationality = player.nationality {
                                Fact(label: labels.country.localized(), value: nationality)
                                    .frame(maxWidth: .infinity, alignment: .leading)
                            }
                            if let height = player.height {
                                Fact(label: labels.height.localized(), value: height)
                                    .frame(maxWidth: .infinity, alignment: .leading)
                            }
                        }
                        Button {
                            model.component.viewModel.onFollowClick(playerId: player.id)
                        } label: {
                            Label(
                                player.followed ? "Following" : "Follow",
                                systemImage: player.followed ? "star.fill" : "star"
                            )
                        }
                    }
                    .padding(.horizontal, 14)
                }
                .tag(index)
            }
        }
        .tabViewStyle(.page)
        .background(Backdrop(url: nil))
        .navigationTitle(labels.title.localized())
        .navigationBarTitleDisplayMode(.inline)
    }
}
