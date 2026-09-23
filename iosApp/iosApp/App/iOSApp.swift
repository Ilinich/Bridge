import SwiftUI
import Shared

@main
struct iOSApp: App {

    /// The graph starts before any screen asks it for anything — a native UI has no single entry
    /// point that could do it lazily, and BGTaskScheduler wants its registration this early.
    init() {
        IosBridge.shared.start()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
