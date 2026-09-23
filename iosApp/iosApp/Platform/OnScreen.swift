import SwiftUI

private struct IsOnScreenKey: EnvironmentKey {
    static let defaultValue = true
}

extension EnvironmentValues {

    /// Whether the screen reading this is the one the user is looking at.
    ///
    /// All four tabs stay alive so each keeps its place in its own stack — that is deliberate, and
    /// it is also why an animation cannot assume it is visible. Anything that runs per frame reads
    /// this and stops when it is false.
    var isOnScreen: Bool {
        get { self[IsOnScreenKey.self] }
        set { self[IsOnScreenKey.self] = newValue }
    }
}
