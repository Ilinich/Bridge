import SwiftUI
import Shared

struct MatchDetailScreen: View {

    @StateObject private var model: ScreenModel<ImplMatchDetailUiState>
    private let component: ImplMatchDetailComponent

    init(matchId: String) {
        let component = IosBridge.shared.matchDetail(matchId: matchId)
        self.component = component
        _model = StateObject(
            wrappedValue: ScreenModel(flow: component.state, close: component.close)
        )
    }

    var body: some View {
        let state = model.state
        let labels = state.labels
        ScrollView {
            VStack(spacing: 18) {
                if let match = state.match {
                    Panel(title: nil) {
                        HStack {
                            Text(match.homeName).foregroundStyle(.white)
                            Spacer()
                            Text(match.scoreline.localized())
                                .font(.system(.title2, design: .monospaced))
                                .foregroundStyle(.white)
                            Spacer()
                            Text(match.awayName).foregroundStyle(.white)
                        }
                        Fact(label: labels.kickoff.localized(), value: match.kickoff)
                        Text(match.round.localized()).font(.caption).foregroundStyle(.secondary)
                    }
                } else if !state.isLoading {
                    Text(labels.notFound.localized()).foregroundStyle(.secondary)
                }
            }
            .padding(20)
        }
        .screenBackground()
        .navigationTitle(labels.title.localized())
        .navigationBarTitleDisplayMode(.inline)
    }
}
