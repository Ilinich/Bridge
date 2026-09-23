import SwiftUI

/// Screen chrome: the backdrop behind the scroll, the same insets the Compose screens use, and
/// room at the bottom for the floating bar.
///
/// The scroll runs into the bottom safe area on purpose. Stopping it above the home indicator left
/// a dead band under the bar and clipped the last row against it; content is meant to pass beneath
/// the capsule, which is what the fade over it is for.
struct Screen<Content: View>: View {

    var backdropUrl: String? = nil
    @ViewBuilder let content: Content

    @EnvironmentObject private var navigation: AppNavigation

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 12) { content }
                .padding(.horizontal, 14)
                .padding(.top, 16)
                .padding(.bottom, BarClearance)
        }
        .scrollIndicators(.hidden)
        .background(Backdrop(url: backdropUrl))
        .overlay(alignment: .top) { ScrollEdgeFade(edge: .top, height: 96) }
        .overlay(alignment: .bottom) { ScrollEdgeFade(edge: .bottom) }
        // The bar overlays this scroll rather than the shell around it. A material blurs what is
        // behind it in its own rendering context, and a TabView's pages are not that: hung outside
        // them, the capsule had nothing to sample and read as a flat slab. The Compose bar is a
        // sibling of the backdrop for the same reason.
        .ignoresSafeArea(edges: .bottom)
        // After the safe-area edge, so the bar is anchored to the page rather than to whatever
        // height the scroll happens to have been given inside it.
        .overlay(alignment: .bottom) { TabBar(selection: $navigation.tab) }
        .toolbarBackground(.hidden, for: .navigationBar)
    }
}

/// Bar height, its inset and the home indicator, plus the room a last row needs to clear the bar.
private let BarClearance: CGFloat = 96
