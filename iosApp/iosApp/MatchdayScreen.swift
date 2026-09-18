import SwiftUI
import Shared

/// The Swift half of one screen: it owns the shared state holder and turns its flow into
/// something SwiftUI redraws on.
///
/// The Kotlin side decides *what* the screen says — including which words, as descriptions it does
/// not resolve. This decides what that looks like.
@MainActor
final class MatchdayModel: ObservableObject {

    @Published private(set) var state: ImplMatchdayUiState

    let component: ImplMatchdayComponent
    private var observation: Task<Void, Never>?

    init() {
        let component = IosBridge.shared.matchday()
        self.component = component
        self.state = component.state.value
        observation = Task { [weak self] in
            for await next in component.state {
                self?.state = next
            }
        }
    }

    func retry() {
        component.viewModel.retry()
    }

    /// SwiftUI has no ViewModelStore: whoever holds the component ends its work.
    deinit {
        observation?.cancel()
        component.close()
    }
}

struct MatchdayScreen: View {

    @StateObject private var model = MatchdayModel()

    var body: some View {
        let state = model.state
        ScrollView {
            VStack(alignment: .leading, spacing: 18) {
                if let next = state.nextMatch {
                    NextMatchCard(component: model.component, match: next, labels: state.labels)
                } else if state.nextMatchFailed {
                    Text(state.labels.fixtureFailed.localized()).foregroundStyle(.secondary)
                    Button(action: model.retry) { Text("Retry") }
                }

                if !state.following.isEmpty {
                    Panel(title: state.labels.following.localized()) {
                        ForEach(state.following, id: \.id) { player in
                            Row(leading: "★", title: player.name, trailing: nil)
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
                        HStack(alignment: .top, spacing: 18) {
                            Fact(label: state.labels.arena.localized(), value: stadium.arena)
                            Fact(label: state.labels.capacity.localized(), value: stadium.capacity)
                            Fact(label: state.labels.founded.localized(), value: stadium.founded)
                        }
                    }
                }

                if state.isOffline {
                    Text("Offline").font(.footnote).foregroundStyle(.secondary)
                }
            }
            .padding(20)
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .background(Color(red: 0.04, green: 0.07, blue: 0.14))
        .preferredColorScheme(.dark)
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
                Badge(url: match.homeBadgeUrl, code: match.homeCode)
                Text("—").foregroundStyle(.secondary)
                Badge(url: match.awayBadgeUrl, code: match.awayCode)
            }
            Text(match.kickoffText).font(.headline).foregroundStyle(.white)
            Text(labels.kickoffLocal.localized()).font(.caption2).foregroundStyle(.secondary)
            Countdown(component: component, kickoffMillis: match.kickoffMillis, labels: labels)
        }
        .padding(18)
        .frame(maxWidth: .infinity)
        .background(Color.white.opacity(0.06), in: RoundedRectangle(cornerRadius: 18))
    }
}

/// The clock ticks on the Swift side; the shared code only says when kick-off is.
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

private struct Panel<Content: View>: View {

    let title: String
    @ViewBuilder let content: Content

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title).font(.caption).foregroundStyle(.secondary)
            VStack(spacing: 8) { content }
                .padding(14)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(Color.white.opacity(0.06), in: RoundedRectangle(cornerRadius: 16))
        }
    }
}

private struct Row: View {

    let leading: String
    let title: String
    let trailing: String?

    var body: some View {
        HStack {
            Text(leading).font(.caption).foregroundStyle(.blue)
            Text(title).foregroundStyle(.white)
            Spacer()
            if let trailing {
                Text(trailing).font(.system(.body, design: .monospaced)).foregroundStyle(.white)
            }
        }
    }
}

private struct Fact: View {

    let label: String
    let value: String

    var body: some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(label).font(.caption2).foregroundStyle(.secondary)
            Text(value).foregroundStyle(.white)
        }
    }
}

private struct Badge: View {

    let url: String?
    let code: String

    var body: some View {
        AsyncImage(url: url.flatMap(URL.init(string:))) { image in
            image.resizable().scaledToFit()
        } placeholder: {
            Text(code).font(.caption2).foregroundStyle(.secondary)
        }
        .frame(width: 46, height: 46)
    }
}
