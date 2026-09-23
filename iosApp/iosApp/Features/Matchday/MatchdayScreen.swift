import SwiftUI
import Shared

struct MatchdayScreen: View {

    @StateObject private var model = ScreenModel(
        component: IosBridge.shared.matchday(),
        state: { $0.state },
        close: { $0.close() }
    )

    var body: some View {
        let state = model.state
        let labels = state.labels
        let actions = model.component.viewModel

        Screen(backdropUrl: state.backdropUrl) {
            // The club is what the screen is about; a fixture that failed is the hero card's own
            // message, the way it is on the Compose side.
            Group {
                HeroCard(component: model.component, state: state)

                if !state.following.isEmpty {
                    Section(title: labels.following.localized()) {
                        VStack(spacing: 6) {
                            ForEach(state.following, id: \.id) { player in
                                Button { actions.onFollowedPlayerClick(playerId: player.id) } label: {
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
                        Button { actions.onStadiumClick() } label: {
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
                onRetry: { actions.retry() }
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
