import SwiftUI
import Shared

struct SeasonScreen: View {

    @StateObject private var model: ScreenModel<ImplSeasonUiState>
    private let component: ImplSeasonComponent

    init() {
        let component = IosBridge.shared.season()
        self.component = component
        _model = StateObject(
            wrappedValue: ScreenModel(flow: component.state, close: component.close)
        )
    }

    var body: some View {
        let state = model.state
        ScrollView {
            LazyVStack(alignment: .leading, spacing: 18) {
                ForEach(state.rounds, id: \.number) { round in
                    Panel(title: round.title.localized()) {
                        ForEach(round.matches, id: \.id) { match in
                            Button {
                                component.viewModel.onMatchClick(matchId: match.id)
                            } label: {
                                HStack {
                                    Text(match.teams.localized())
                                        .foregroundStyle(match.highlighted ? .white : .secondary)
                                    Spacer()
                                    Text(match.trailing.localized())
                                        .font(.system(.body, design: .monospaced))
                                        .foregroundStyle(match.hasScore ? .white : .secondary)
                                }
                            }
                        }
                    }
                }
            }
            .padding(20)
        }
        .screenBackground()
        .navigationTitle("Season")
    }
}
