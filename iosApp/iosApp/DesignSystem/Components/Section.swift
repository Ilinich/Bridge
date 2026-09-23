import SwiftUI

/// A titled section: the monospaced caption, then the solid surface it labels.
struct Section<Content: View>: View {

    let title: String
    @ViewBuilder let content: Content

    var body: some View {
        VStack(alignment: .leading, spacing: 7) {
            Text(title).labelStyle()
            content
        }
    }
}
