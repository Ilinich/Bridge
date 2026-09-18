import SwiftUI
import Shared

private let CutoutParallax: CGFloat = 40
private let PageFadeStrength: CGFloat = 0.4

struct PlayerScreen: View {

    let playerId: String

    @StateObject private var model: ScreenModel<ImplPlayerComponent, ImplPlayerUiState>

    /// Which page is in view. Seeded with the player that was tapped rather than set on appear:
    /// set later, the pager has already settled on the first page and the title names someone the
    /// screen is not showing.
    @State private var shown: String?

    init(playerId: String) {
        self.playerId = playerId
        _shown = State(initialValue: playerId)
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
                                    onFollow: { model.component.viewModel.onFollowClick(playerId: $0) }
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
                        pager.scrollTo(playerId, anchor: .center)
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
        players.first { $0.id == (shown ?? playerId) }
    }

    private func index(in players: [ImplPlayerPageUi]) -> Int {
        players.firstIndex { $0.id == (shown ?? playerId) } ?? 0
    }
}

private struct PlayerPage: View {

    let player: ImplPlayerPageUi
    let labels: ImplPlayerLabels
    let width: CGFloat
    let onFollow: (String) -> Void

    var body: some View {
        // The page's own offset drives the parallax, read where it is drawn rather than kept in
        // state: the cutout slides and fades while the panel under it stays put.
        GeometryReader { page in
            let fraction = width == 0 ? 0 : -page.frame(in: .scrollView).minX / width

            ZStack(alignment: .bottom) {
                AsyncImage(url: player.cutoutUrl.flatMap(URL.init(string:))) { image in
                    image.resizable().scaledToFit()
                } placeholder: {
                    Color.clear
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .padding(.top, 70)
                .padding(.bottom, 190)
                .offset(x: fraction * CutoutParallax)
                .opacity(1 - min(abs(fraction), 1) * PageFadeStrength)

                GlassPanel {
                    PlayerFacts(player: player, labels: labels, onFollow: onFollow)
                }
                .padding(.horizontal, 14)
                .padding(.bottom, 68)
            }
        }
    }
}

private struct PlayerFacts: View {

    let player: ImplPlayerPageUi
    let labels: ImplPlayerLabels
    let onFollow: (String) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack(alignment: .firstTextBaseline) {
                Text(player.name)
                    .font(.system(size: 22, weight: .heavy))
                    .foregroundStyle(Color.textPrimary)
                Spacer(minLength: 8)
                Button { onFollow(player.id) } label: {
                    Image(systemName: player.followed ? "star.fill" : "star")
                        .font(.system(size: 17))
                        .foregroundStyle(player.followed ? Color.clubBright : Color.textMuted)
                }
                .buttonStyle(.plain)
            }
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
}

/// The pager's dots: five points wide when idle, a fourteen-point capsule for the page in view.
private struct PageDots: View {

    let count: Int
    let selected: Int

    var body: some View {
        HStack(spacing: 5) {
            ForEach(0..<max(count, 0), id: \.self) { index in
                Capsule()
                    .fill(index == selected ? Color.clubBright : Color.line)
                    .frame(width: index == selected ? 14 : 5, height: 5)
            }
        }
        .animation(.easeInOut(duration: 0.2), value: selected)
    }
}
