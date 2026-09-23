import SwiftUI
import Shared

/// A screen pushed on top of a tab.
enum Push: Hashable {
    case player(String)
    case matchDetail(String)
}

enum Tab: Int, Hashable {
    case matchday, season, squad, club
}

/// The back stacks, and the one place that obeys the shared router.
///
/// The router says *where to go*; what that means is decided here, against real UIKit stacks. The
/// Compose host makes the same decisions against its own — including the rule that a tab root
/// selects its tab rather than being pushed onto whatever is open.
@MainActor
final class AppNavigation: ObservableObject {

    @Published var tab: Tab = .matchday
    @Published var paths: [Tab: [Push]] = [:]

    private var observation: Task<Void, Never>?

    /// `observing: false` builds a host that obeys commands handed to it directly — which is what
    /// a test does, and the only way to check the stacks without a running graph.
    init(observing: Bool = true) {
        guard observing else { return }
        observation = Task { [weak self] in
            for await command in IosBridge.shared.navigation {
                self?.apply(command)
            }
        }
    }

    deinit {
        observation?.cancel()
    }

    func path(for tab: Tab) -> Binding<[Push]> {
        Binding(
            get: { [weak self] in self?.paths[tab] ?? [] },
            set: { [weak self] value in self?.paths[tab] = value }
        )
    }

    func apply(_ command: IosNavigation) {
        switch onEnum(of: command) {
        case .matchday: tab = .matchday
        case .season: tab = .season
        case .squad: tab = .squad
        case .club: tab = .club
        case .player(let destination): push(.player(destination.playerId))
        case .matchDetail(let destination): push(.matchDetail(destination.matchId))
        case .up: pop()
        }
    }

    private func push(_ screen: Push) {
        paths[tab, default: []].append(screen)
    }

    private func pop() {
        guard var stack = paths[tab], !stack.isEmpty else { return }
        stack.removeLast()
        paths[tab] = stack
    }
}
