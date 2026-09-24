import SwiftUI
import Shared

struct MatchdayScreen: View {

    @StateObject private var model = ScreenModel(
        component: IosBridge.shared.matchday(),
        state: { $0.state },
        close: { $0.close() }
    )

    var body: some View {
        let component = model.component
        MatchdayContent(
            state: model.state,
            remaining: { now in
                let kickoff = model.state.nextMatch?.kickoffMillis ?? now
                return component.countdown(nowMillis: now, kickoffMillis: kickoff)
            },
            onFollowedPlayer: { component.viewModel.onFollowedPlayerClick(playerId: $0) },
            onStadium: { component.viewModel.onStadiumClick() },
            onRetry: { component.viewModel.retry() }
        )
    }
}

/// Everything this screen draws, and nothing about where the state came from — which is what lets
/// a preview draw it from `IosPreviews` without a graph behind it.
struct MatchdayContent: View {

    let state: ImplMatchdayUiState
    let remaining: (Int64) -> ImplCountdown
    let onFollowedPlayer: (String) -> Void
    let onStadium: () -> Void
    let onRetry: () -> Void

    var body: some View {
        let labels = state.labels

        Screen(backdropUrl: state.backdropUrl) {
            // The club is what the screen is about; a fixture that failed is the hero card's own
            // message, the way it is on the Compose side.
            Group {
                HeroCard(state: state, remaining: remaining)

                if !state.following.isEmpty {
                    Section(title: labels.following.localized()) {
                        VStack(spacing: 6) {
                            ForEach(state.following, id: \.id) { player in
                                Button { onFollowedPlayer(player.id) } label: {
                                    FollowedRow(name: player.name)
                                }
                            }
                        }
                    }
                }

                if let recent = state.recent {
                    Section(title: labels.recent.localized()) {
                        RecentRow(match: recent)
                    }
                }

                if let stadium = state.stadium {
                    Section(title: labels.stadium.localized()) {
                        Button(action: onStadium) {
                            StadiumRow(stadium: stadium, labels: labels)
                        }
                        .buttonStyle(.plain)
                    }
                }

                if state.isOffline {
                    OfflineNotice()
                }
            }
            .loadable(
                isLoading: state.isLoading && !state.hasClub,
                hasFailed: state.error != nil && !state.hasClub,
                onRetry: onRetry
            )
        }
    }
}

private struct FollowedRow: View {

    let name: String

    var body: some View {
        SurfaceRow {
            Image(systemName: "star.fill")
                .font(.glyphSmall)
                .foregroundStyle(Color.clubBright)
            Text(name)
                .font(.label)
                .foregroundStyle(Color.textPrimary)
                .lineLimit(1)
            Spacer(minLength: 0)
        }
    }
}

private struct RecentRow: View {

    let match: ImplRecentMatchUi

    var body: some View {
        SurfaceRow {
            Badge(url: match.awayBadgeUrl, code: match.awayCode, size: 22)
            VStack(alignment: .leading, spacing: 2) {
                Text(match.teams.localized())
                    .font(.label)
                    .foregroundStyle(Color.textPrimary)
                    .lineLimit(1)
                Text(match.competition).labelStyle()
            }
            Spacer(minLength: 8)
            if let score = match.score {
                Text(score.localized())
                    .font(.figure)
                    .foregroundStyle(Color.textPrimary)
            }
        }
    }
}

private struct StadiumRow: View {

    let stadium: ImplStadiumUi
    let labels: ImplMatchdayLabels

    var body: some View {
        // Equal columns. The Compose row weights them 1.7 / 1 / 1, and the obvious SwiftUI
        // translation — containerRelativeFrame — asks the scroll container for its width while the
        // container is asking this row for its height, and the layout pass never terminates.
        SurfaceRow(spacing: 6) {
            Fact(label: labels.arena.localized(), value: stadium.arena)
                .frame(maxWidth: .infinity, alignment: .leading)
            Fact(label: labels.capacity.localized(), value: stadium.capacity)
                .frame(maxWidth: .infinity, alignment: .leading)
            Fact(label: labels.founded.localized(), value: stadium.founded)
                .frame(maxWidth: .infinity, alignment: .leading)
        }
    }
}

#Preview("Matchday") {
    PreviewHost {
        MatchdayContent(
            state: IosPreviews.shared.matchday(),
            remaining: { _ in ImplCountdown(days: 16, hours: 8, minutes: 12, seconds: 15) },
            onFollowedPlayer: { _ in },
            onStadium: {},
            onRetry: {}
        )
    }
}

#Preview("Matchday, offline and without a fixture") {
    PreviewHost {
        MatchdayContent(
            state: IosPreviews.shared.matchdayEmpty(),
            remaining: { _ in ImplCountdown(days: 0, hours: 0, minutes: 0, seconds: 0) },
            onFollowedPlayer: { _ in },
            onStadium: {},
            onRetry: {}
        )
    }
}
