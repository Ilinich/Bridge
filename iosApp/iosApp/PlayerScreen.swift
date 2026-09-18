import SwiftUI
import Shared

struct PlayerScreen: View {

    let playerId: String

    @StateObject private var model: ScreenModel<ImplPlayerUiState>
    private let component: ImplPlayerComponent

    init(playerId: String) {
        self.playerId = playerId
        let component = IosBridge.shared.player()
        self.component = component
        _model = StateObject(
            wrappedValue: ScreenModel(flow: component.state, close: component.close)
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
                        Text(player.name).font(.title2).foregroundStyle(.white)
                        Panel(title: nil) {
                            HStack(alignment: .top, spacing: 18) {
                                if let number = player.shirtNumber {
                                    Fact(label: labels.number.localized(), value: number)
                                }
                                if let position = player.position {
                                    Fact(label: labels.position.localized(), value: position)
                                }
                                if let nationality = player.nationality {
                                    Fact(label: labels.country.localized(), value: nationality)
                                }
                                if let height = player.height {
                                    Fact(label: labels.height.localized(), value: height)
                                }
                            }
                        }
                        Button {
                            component.viewModel.onFollowClick(playerId: player.id)
                        } label: {
                            Label(
                                player.followed ? "Following" : "Follow",
                                systemImage: player.followed ? "star.fill" : "star"
                            )
                        }
                    }
                    .padding(20)
                }
                .tag(index)
            }
        }
        .tabViewStyle(.page)
        .screenBackground()
        .navigationTitle(labels.title.localized())
        .navigationBarTitleDisplayMode(.inline)
    }
}
