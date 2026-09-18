import SwiftUI
import Shared

/// One observable for every screen: it holds a shared component, republishes its state and ends
/// its work when SwiftUI lets go.
///
/// SwiftUI has no ViewModelStore, so this deinit is the whole lifecycle contract.
@MainActor
final class ScreenModel<State>: ObservableObject {

    @Published private(set) var state: State

    private let onClose: () -> Void
    private var observation: Task<Void, Never>?

    init(flow: SkieSwiftStateFlow<State>, close: @escaping () -> Void) {
        self.state = flow.value
        self.onClose = close
        observation = Task { [weak self] in
            for await next in flow {
                self?.state = next
            }
        }
    }

    deinit {
        observation?.cancel()
        onClose()
    }
}
