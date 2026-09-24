import SwiftUI
import Shared

/// What a screen needs around it in a preview: the navigation host its tab bar reads, and the
/// app's own ground and tint.
///
/// `observing: false` keeps this host off the shared router — a preview has no graph to listen to,
/// and the states it draws come from `IosPreviews` rather than from a running feature.
struct PreviewHost<Content: View>: View {

    @ViewBuilder let content: Content

    var body: some View {
        content
            .environmentObject(AppNavigation(observing: false))
            .environment(\.usesBundledImages, true)
            .background(Color.ground)
            .preferredColorScheme(.dark)
            .tint(.clubBright)
    }
}
