import AVKit
import SwiftUI
import Shared

struct ClubScreen: View {

    @StateObject private var model = ScreenModel(
        component: IosBridge.shared.club(),
        state: { $0.state },
        close: { $0.close() }
    )

    var body: some View {
        let component = model.component
        ClubContent(
            state: model.state,
            clipUrl: component.clipUrl,
            onVideoStarted: { component.onVideoStarted() },
            onRetry: { component.viewModel.retry() }
        )
    }
}

/// Everything this screen draws, and nothing about where the state came from — which is what lets
/// a preview draw it from `IosPreviews` without a graph behind it.
struct ClubContent: View {

    let state: ImplClubUiState
    let clipUrl: String
    let onVideoStarted: () -> Void
    let onRetry: () -> Void

    /// One player for the life of the screen.
    ///
    /// It used to be a computed property, which means SwiftUI built a new AVPlayer on every pass
    /// over the body: a fresh download each time and a position that could never be kept. The
    /// url came from the shared code through a force unwrap, too.
    @State private var player = ClipPlayer()

    var body: some View {
        let labels = state.labels

        Screen(backdropUrl: state.club?.backdropUrl) {
            Group {
                if let club = state.club {
                    ClubHeader(club: club, labels: labels)
                }

                Section(title: labels.media.localized()) {
                    VideoPlayer(player: player.avPlayer)
                        .frame(height: 180)
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                        .onAppear {
                            player.load(clipUrl)
                            onVideoStarted()
                        }
                }

                if let summary = state.club?.summary {
                    Section(title: labels.about.localized()) {
                        SurfaceRow {
                            Text(summary)
                                .font(.prose)
                                .foregroundStyle(Color.textMuted)
                                .lineSpacing(3)
                        }
                    }
                }

                if let ground = state.ground {
                    Section(title: labels.ground.localized()) {
                        Ground(ground: ground, labels: labels)
                    }
                }

                if let links = state.club?.links, !links.isEmpty {
                    Section(title: labels.links.localized()) {
                        Links(links: links)
                    }
                }
            }
            .loadable(
                isLoading: state.isLoading && state.club == nil,
                hasFailed: state.error != nil && state.club == nil,
                onRetry: onRetry
            )
        }
    }
}

private struct ClubHeader: View {

    let club: ImplClubUi
    let labels: ImplClubLabels

    var body: some View {
        GlassPanel {
            VStack(spacing: 9) {
                Badge(url: club.badgeUrl, code: club.code, size: 68)
                Text(club.name)
                    .font(.display)
                    .foregroundStyle(Color.textPrimary)
                if let nicknames = club.nicknames {
                    Text(nicknames).labelStyle().multilineTextAlignment(.center)
                }
                HStack(alignment: .top, spacing: 18) {
                    if let founded = club.founded {
                        Fact(label: labels.founded.localized(), value: founded)
                    }
                    Colours(colours: club.colours, label: labels.colours.localized())
                }
            }
        }
    }
}

private struct Ground: View {

    let ground: ImplGroundUi
    let labels: ImplClubLabels

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            SurfaceRow {
                Text(ground.name)
                    .font(.label)
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

private struct Links: View {

    let links: [ImplClubLinkUi]

    var body: some View {
        VStack(spacing: 6) {
            ForEach(links, id: \.url) { link in
                if let url = URL(string: link.url) {
                    Link(destination: url) {
                        SurfaceRow {
                            Text(link.label.localized())
                                .font(.label)
                                .foregroundStyle(Color.clubBright)
                            Spacer(minLength: 0)
                        }
                    }
                }
            }
        }
    }
}

private struct Colours: View {

    let colours: [String]
    let label: String

    var body: some View {
        let parsed = colours.compactMap(Color.init(hex:))
        if !parsed.isEmpty {
            VStack(alignment: .leading, spacing: 4) {
                Text(label).labelStyle()
                HStack(spacing: 6) {
                    ForEach(Array(parsed.enumerated()), id: \.offset) { _, colour in
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

#Preview("Club") {
    PreviewHost {
        ClubContent(
            state: IosPreviews.shared.club(),
            clipUrl: "",
            onVideoStarted: {},
            onRetry: {}
        )
    }
}
