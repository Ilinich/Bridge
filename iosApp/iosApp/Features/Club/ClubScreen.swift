import AVKit
import SwiftUI
import Shared

struct ClubScreen: View {

    @StateObject private var model = ScreenModel(
        component: IosBridge.shared.club(),
        state: { $0.state },
        close: { $0.close() }
    )

    /// One player for the life of the screen.
    ///
    /// It used to be a computed property, which means SwiftUI built a new AVPlayer on every pass
    /// over the body: a fresh download each time and a position that could never be kept. The
    /// url came from the shared code through a force unwrap, too.
    @State private var player = ClipPlayer()

    var body: some View {
        let state = model.state
        let labels = state.labels
        Screen(backdropUrl: state.club?.backdropUrl) {
            LoadableView(
                isLoading: state.isLoading && state.club == nil,
                hasFailed: state.error != nil && state.club == nil,
                onRetry: { model.component.viewModel.retry() }
            ) {
            if let club = state.club {
                GlassPanel {
                    VStack(spacing: 9) {
                        Badge(url: club.badgeUrl, code: club.code, size: 68)
                        Text(club.name)
                            .font(.system(size: 28, weight: .heavy))
                            .foregroundStyle(Color.textPrimary)
                        if let nicknames = club.nicknames {
                            Text(nicknames).labelStyle().multilineTextAlignment(.center)
                        }
                        HStack(alignment: .top, spacing: 18) {
                            if let founded = club.founded {
                                Fact(label: labels.founded.localized(), value: founded)
                            }
                            Colours(club: club, label: labels.colours.localized())
                        }
                    }
                }
            }

            Section(title: labels.media.localized()) {
                VStack(spacing: 10) {
                    VideoPlayer(player: player.avPlayer)
                        .frame(height: 180)
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                        .onAppear {
                            player.load(model.component.clipUrl)
                            model.component.onVideoStarted()
                        }
                }
            }

            if let summary = state.club?.summary {
                Section(title: labels.about.localized()) {
                    SurfaceRow {
                        Text(summary)
                            .font(.system(size: 14))
                            .foregroundStyle(Color.textMuted)
                            .lineSpacing(3)
                    }
                }
            }

            if let ground = state.ground {
                Section(title: labels.ground.localized()) {
                    VStack(alignment: .leading, spacing: 6) {
                        SurfaceRow {
                            Text(ground.name)
                                .font(.labelLarge)
                                .foregroundStyle(Color.textPrimary)
                            Spacer(minLength: 0)
                        }
                        SurfaceRow(spacing: 6) {
                            if let capacity = ground.capacity {
                                Fact(label: labels.capacity.localized(), value: capacity)
                                    .frame(maxWidth: .infinity, alignment: .leading)
                            }
                            if let opened = ground.opened {
                                Fact(label: labels.opened.localized(), value: opened)
                                    .frame(maxWidth: .infinity, alignment: .leading)
                            }
                            if let location = ground.location {
                                Fact(label: labels.location.localized(), value: location)
                                    .frame(maxWidth: .infinity, alignment: .leading)
                            }
                        }
                    }
                }
            }

            if let club = state.club, !club.links.isEmpty {
                Section(title: labels.links.localized()) {
                    VStack(spacing: 6) {
                        ForEach(club.links, id: \.url) { link in
                            if let url = URL(string: link.url) {
                                Link(destination: url) {
                                    SurfaceRow {
                                        Text(link.label.localized())
                                            .font(.labelLarge)
                                            .foregroundStyle(Color.clubBright)
                                        Spacer(minLength: 0)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            }
        }
    }


}

private struct Colours: View {

    let club: ImplClubUi
    let label: String

    var body: some View {
        let colours = club.colours.compactMap(Color.init(hex:))
        if !colours.isEmpty {
            VStack(alignment: .leading, spacing: 4) {
                Text(label).labelStyle()
                HStack(spacing: 6) {
                    ForEach(Array(colours.enumerated()), id: \.offset) { _, colour in
                        Circle()
                            .fill(colour)
                            .frame(width: 18, height: 18)
                    }
                }
            }
        }
    }
}

extension Color {

    /// The feature hands colours over as the API spells them; parsing belongs to whoever draws.
    init?(hex: String) {
        var value = hex.trimmingCharacters(in: .whitespaces)
        if value.hasPrefix("#") { value.removeFirst() }
        guard value.count == 6, let number = UInt32(value, radix: 16) else { return nil }
        self.init(
            red: Double((number >> 16) & 0xFF) / 255,
            green: Double((number >> 8) & 0xFF) / 255,
            blue: Double(number & 0xFF) / 255
        )
    }
}
