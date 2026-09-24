import SwiftUI
import Shared

struct MatchDetailScreen: View {

    @StateObject private var model: ScreenModel<ImplMatchDetailComponent, ImplMatchDetailUiState>

    init(matchId: String) {
        _model = StateObject(
            wrappedValue: ScreenModel(
                component: IosBridge.shared.matchDetail(matchId: matchId),
                state: { $0.state },
                close: { $0.close() }
            )
        )
    }

    var body: some View {
        MatchDetailContent(state: model.state)
    }
}

/// Everything this screen draws, and nothing about where the state came from — which is what lets
/// a preview draw it from `IosPreviews` without a graph behind it.
struct MatchDetailContent: View {

    let state: ImplMatchDetailUiState

    var body: some View {
        let labels = state.labels

        ScrollView {
            VStack(spacing: 18) {
                if let match = state.match {
                    GlassPanel {
                        HStack {
                            TeamName(match.homeName)
                            Spacer()
                            Text(match.scoreline.localized())
                                .font(.figure)
                                .foregroundStyle(Color.textPrimary)
                            Spacer()
                            TeamName(match.awayName)
                        }
                        Fact(label: labels.kickoff.localized(), value: match.kickoff)
                            .frame(maxWidth: .infinity, alignment: .leading)
                        Text(match.round.localized()).labelStyle()
                    }
                } else if !state.isLoading {
                    Text(labels.notFound.localized()).foregroundStyle(Color.textMuted)
                }
            }
            .padding(.horizontal, 14)
        }
        .background(Backdrop(url: nil))
        .navigationTitle(labels.title.localized())
        .navigationBarTitleDisplayMode(.inline)
    }
}

private struct TeamName: View {

    let name: String

    init(_ name: String) {
        self.name = name
    }

    var body: some View {
        Text(name)
            .font(.subheading)
            .foregroundStyle(Color.textPrimary)
    }
}

#Preview("Match detail") {
    PreviewHost {
        NavigationStack {
            MatchDetailContent(state: IosPreviews.shared.matchDetail())
        }
    }
}
