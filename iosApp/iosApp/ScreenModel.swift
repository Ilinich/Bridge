import SwiftUI
import Shared

/// One observable for every screen: it owns a shared component, republishes its state and ends
/// its work when SwiftUI lets go.
///
/// The component is built **inside** this initialiser and never in a view's `init`. A SwiftUI view
/// is a struct that gets rebuilt constantly; building a component there makes one per rebuild,
/// each with its own scope, and the ones SwiftUI discards close on deinit — cancelling the very
/// scope the surviving model is waiting on. The screen then sits at "loading" for ever, which is
/// exactly what it did.
@MainActor
final class ScreenModel<Component: AnyObject, State>: ObservableObject {

    let component: Component

    @Published private(set) var state: State

    private let onClose: (Component) -> Void
    private var observation: Task<Void, Never>?

    init(
        component: @autoclosure () -> Component,
        state: (Component) -> SkieSwiftStateFlow<State>,
        close: @escaping (Component) -> Void
    ) {
        let made = component()
        let flow = state(made)
        self.component = made
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
        onClose(component)
    }
}
