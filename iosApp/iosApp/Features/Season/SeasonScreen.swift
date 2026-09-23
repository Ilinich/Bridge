import SwiftUI
import Shared

struct SeasonScreen: View {

    @StateObject private var model = ScreenModel(
        component: IosBridge.shared.season(),
        state: { $0.state },
        close: { $0.close() }
    )

    var body: some View {
        let state = model.state
        let actions = model.component.viewModel

        Screen {
            Group {
                if state.isOffline {
                    OfflineNotice()
                }

                LazyVStack(alignment: .leading, spacing: 18) {
                    ForEach(state.rounds, id: \.number) { round in
                        Section(title: round.title.localized()) {
                            VStack(spacing: 6) {
                                ForEach(round.matches, id: \.id) { match in
                                    Button { actions.onMatchClick(matchId: match.id) } label: {
                                        FixtureRow(match: match)
                                    }
                                    .buttonStyle(.plain)
                                }
                            }
                        }
                    }
                }
            }
            .loadable(
                isLoading: state.isLoading && state.rounds.isEmpty,
                hasFailed: state.error != nil && state.rounds.isEmpty,
                onRetry: { actions.retry() }
            )
        }
    }
}

private struct FixtureRow: View {

    let match: ImplFixtureRowUi

    var body: some View {
        SurfaceRow {
            VStack(alignment: .leading, spacing: 2) {
                Text(match.teams.localized())
                    .font(.label)
                    .foregroundStyle(match.highlighted ? Color.textPrimary : Color.textMuted)
                    .lineLimit(1)
                Text(match.day).labelStyle()
            }
            Spacer(minLength: 8)
            Text(match.trailing.localized())
                .font(.figure)
                .foregroundStyle(match.hasScore ? Color.textPrimary : Color.textMuted)
        }
    }
}
