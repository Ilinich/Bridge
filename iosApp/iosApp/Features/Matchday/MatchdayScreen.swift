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
        Screen(backdropUrl: state.backdropUrl) {
            // The club is what the screen is about; a fixture that failed is the hero card's own
            // message, the way it is on the Compose side.
            LoadableView(
                isLoading: state.isLoading && !state.hasClub,
                hasFailed: state.error != nil && !state.hasClub,
                onRetry: { model.component.viewModel.retry() },
                content: {
            HeroCard(component: model.component, state: state)

            if !state.following.isEmpty {
                Section(title: state.labels.following.localized()) {
                    VStack(spacing: 6) {
                        ForEach(state.following, id: \.id) { player in
                            Button {
                                model.component.viewModel.onFollowedPlayerClick(playerId: player.id)
                            } label: {
                                SurfaceRow {
                                    Image(systemName: "star.fill")
                                        .font(.system(size: 13))
                                        .foregroundStyle(Color.clubBright)
                                    Text(player.name)
                                        .font(.labelLarge)
                                        .foregroundStyle(Color.textPrimary)
                                        .lineLimit(1)
                                    Spacer(minLength: 0)
                                }
                            }
                        }
                    }
                }
            }

            if let recent = state.recent {
                Section(title: state.labels.recent.localized()) {
                    SurfaceRow {
                        Badge(url: recent.awayBadgeUrl, code: recent.awayCode, size: 22)
                        VStack(alignment: .leading, spacing: 2) {
                            Text(recent.teams.localized())
                                .font(.labelLarge)
                                .foregroundStyle(Color.textPrimary)
                                .lineLimit(1)
                            Text(recent.competition).labelStyle()
                        }
                        Spacer(minLength: 8)
                        if let score = recent.score {
                            Text(score.localized())
                                .font(.figure)
                                .foregroundStyle(Color.textPrimary)
                        }
                    }
                }
            }

            if let stadium = state.stadium {
                Section(title: state.labels.stadium.localized()) {
                    Button {
                        model.component.viewModel.onStadiumClick()
                    } label: {
                        SurfaceRow(spacing: 6) {
                            // Equal columns. The Compose row weights them 1.7 / 1 / 1, and the
                            // obvious SwiftUI translation — containerRelativeFrame — asks the
                            // scroll container for its width while the container is asking this
                            // row for its height, and the layout pass never terminates.
                            Fact(label: state.labels.arena.localized(), value: stadium.arena)
                                .frame(maxWidth: .infinity, alignment: .leading)
                            Fact(label: state.labels.capacity.localized(), value: stadium.capacity)
                                .frame(maxWidth: .infinity, alignment: .leading)
                            Fact(label: state.labels.founded.localized(), value: stadium.founded)
                                .frame(maxWidth: .infinity, alignment: .leading)
                        }
                    }
                    .buttonStyle(.plain)
                }
            }

            if state.isOffline {
                OfflineNotice()
            }
                }
            )
        }
    }
}
