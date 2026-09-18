import SwiftUI
import Shared

struct MatchdayScreen: View {

    @StateObject private var model: ScreenModel<ImplMatchdayUiState>
    private let component: ImplMatchdayComponent

    init() {
        let component = IosBridge.shared.matchday()
        self.component = component
        _model = StateObject(
            wrappedValue: ScreenModel(flow: component.state, close: component.close)
        )
    }

    var body: some View {
        let state = model.state
        ScrollView {
            VStack(alignment: .leading, spacing: 18) {
                if let next = state.nextMatch {
                    NextMatchCard(component: component, match: next, labels: state.labels)
                } else if state.nextMatchFailed {
                    Panel(title: nil) {
                        Text(state.labels.fixtureFailed.localized()).foregroundStyle(.secondary)
                        Button("Retry") { component.viewModel.retry() }
                    }
                }

                if !state.following.isEmpty {
                    Panel(title: state.labels.following.localized()) {
                        ForEach(state.following, id: \.id) { player in
                            Button {
                                component.viewModel.onFollowedPlayerClick(playerId: player.id)
                            } label: {
                                Row(leading: "★", title: player.name, trailing: nil)
                            }
                        }
                    }
                }

                if let recent = state.recent {
                    Panel(title: state.labels.recent.localized()) {
                        Row(
                            leading: recent.awayCode,
                            title: recent.teams.localized(),
                            trailing: recent.score?.localized()
                        )
                    }
                }

                if let stadium = state.stadium {
                    Panel(title: state.labels.stadium.localized()) {
                        Button {
                            component.viewModel.onStadiumClick()
                        } label: {
                            HStack(alignment: .top, spacing: 18) {
                                Fact(label: state.labels.arena.localized(), value: stadium.arena)
                                Fact(label: state.labels.capacity.localized(), value: stadium.capacity)
                                Fact(label: state.labels.founded.localized(), value: stadium.founded)
                            }
                        }
                    }
                }

                if state.isOffline {
                    Text("Offline").font(.footnote).foregroundStyle(.secondary)
                }
            }
            .padding(20)
        }
        .screenBackground()
        .navigationTitle("Matchday")
    }
}

private struct NextMatchCard: View {

    let component: ImplMatchdayComponent
    let match: ImplNextMatchUi
    let labels: ImplMatchdayLabels

    var body: some View {
        VStack(spacing: 12) {
            HStack {
                Text(match.competition).font(.caption).foregroundStyle(.secondary)
                Spacer()
                if let venue = match.venue {
                    Text(venue).font(.caption).foregroundStyle(.secondary)
                }
            }
            HStack(spacing: 16) {
                Badge(url: match.homeBadgeUrl, code: match.homeCode, size: 46)
                Text("—").foregroundStyle(.secondary)
                Badge(url: match.awayBadgeUrl, code: match.awayCode, size: 46)
            }
            Text(match.kickoffText).font(.headline).foregroundStyle(.white)
            Text(labels.kickoffLocal.localized()).font(.caption2).foregroundStyle(.secondary)
            Countdown(component: component, kickoffMillis: match.kickoffMillis, labels: labels)
        }
        .padding(18)
        .frame(maxWidth: .infinity)
        .background(Color.panel, in: RoundedRectangle(cornerRadius: 18))
    }
}

/// The clock ticks on the Swift side; the shared code only says what a remaining second means.
private struct Countdown: View {

    let component: ImplMatchdayComponent
    let kickoffMillis: Int64
    let labels: ImplMatchdayLabels

    var body: some View {
        TimelineView(.periodic(from: .now, by: 1)) { context in
            let now = Int64(context.date.timeIntervalSince1970 * 1000)
            let left = component.countdown(nowMillis: now, kickoffMillis: kickoffMillis)
            if left.hasStarted {
                Text(labels.kickoffNow.localized()).font(.headline).foregroundStyle(.white)
            } else {
                HStack(spacing: 10) {
                    Cell(value: left.days, label: labels.days.localized())
                    Cell(value: left.hours, label: labels.hours.localized())
                    Cell(value: left.minutes, label: labels.minutes.localized())
                    Cell(value: left.seconds, label: labels.seconds.localized())
                }
            }
        }
    }

    private struct Cell: View {
        let value: Int64
        let label: String
        var body: some View {
            VStack(spacing: 2) {
                Text(String(format: "%02d", value))
                    .font(.system(.title3, design: .monospaced))
                    .foregroundStyle(.white)
                Text(label).font(.caption2).foregroundStyle(.secondary)
            }
            .frame(width: 54, height: 54)
            .background(Color.blue.opacity(0.22), in: RoundedRectangle(cornerRadius: 12))
        }
    }
}
