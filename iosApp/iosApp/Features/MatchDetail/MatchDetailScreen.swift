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
        let state = model.state
        let labels = state.labels
        ScrollView {
            VStack(spacing: 18) {
                if let match = state.match {
                    GlassPanel {
                        HStack {
                            Text(match.homeName).font(.system(size: 17, weight: .semibold)).foregroundStyle(Color.textPrimary)
                            Spacer()
                            Text(match.scoreline.localized())
                                .font(.figure)
                                .foregroundStyle(Color.textPrimary)
                            Spacer()
                            Text(match.awayName).font(.system(size: 17, weight: .semibold)).foregroundStyle(Color.textPrimary)
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
