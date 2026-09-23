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
        Screen {
            if state.isOffline {
                OfflineNotice()
            }
            LoadableView(
                isLoading: state.isLoading && state.rounds.isEmpty,
                hasFailed: state.error != nil && state.rounds.isEmpty,
                onRetry: { model.component.viewModel.retry() },
                content: {
                LazyVStack(alignment: .leading, spacing: 18) {
                    ForEach(state.rounds, id: \.number) { round in
                        Section(title: round.title.localized()) {
                            VStack(spacing: 6) {
                                ForEach(round.matches, id: \.id) { match in
                                    Button {
                                        model.component.viewModel.onMatchClick(matchId: match.id)
                                    } label: {
                                        FixtureRow(match: match)
                                    }
                                    .buttonStyle(.plain)
                                }
                            }
                        }
                    }
                }
                }
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
                    .font(.labelLarge)
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
