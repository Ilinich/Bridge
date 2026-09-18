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
        let players = state.players

        ZStack {
            // The same club wash the squad cards carry, here across the whole page.
            LinearGradient(
                colors: [Color.club, Color.ground],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            TabView(selection: selection(in: players)) {
                ForEach(players, id: \.id) { player in
                    PlayerPage(player: player, labels: state.labels)
                        .tag(player.id)
                }
            }
            .tabViewStyle(.page(indexDisplayMode: .always))
            .indexViewStyle(.page(backgroundDisplayMode: .never))
        }
        .navigationTitle(current(in: players)?.name ?? state.labels.title.localized())
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                if let player = current(in: players) {
                    Button {
                        model.component.viewModel.onFollowClick(playerId: player.id)
                    } label: {
                        Image(systemName: player.followed ? "star.fill" : "star")
                            .foregroundStyle(Color.clubBright)
                    }
                }
            }
        }
    }

    @State private var shown: String?

    private func selection(in players: [ImplPlayerPageUi]) -> Binding<String> {
        Binding(
            get: { shown ?? playerId },
            set: { shown = $0 }
        )
    }

    private func current(in players: [ImplPlayerPageUi]) -> ImplPlayerPageUi? {
        players.first { $0.id == (shown ?? playerId) }
    }
}

private struct PlayerPage: View {

    let player: ImplPlayerPageUi
    let labels: ImplPlayerLabels

    var body: some View {
        VStack(spacing: 0) {
            AsyncImage(url: player.cutoutUrl.flatMap(URL.init(string:))) { image in
                image.resizable().scaledToFit()
            } placeholder: {
                Color.clear
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .bottom)

            GlassPanel {
                VStack(alignment: .leading, spacing: 10) {
                    Text(player.name)
                        .font(.system(size: 22, weight: .heavy))
                        .foregroundStyle(Color.textPrimary)
                        .frame(maxWidth: .infinity, alignment: .leading)
                    HStack(alignment: .top, spacing: 6) {
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
                }
            }
            .padding(.horizontal, 14)
            .padding(.bottom, 74)
        }
    }
}
