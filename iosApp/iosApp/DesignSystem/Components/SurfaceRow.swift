import SwiftUI

/// The row surface every section uses: solid, not frosted — the glass is for the hero and the bars.
struct SurfaceRow<Content: View>: View {

    var spacing: CGFloat = 9
    @ViewBuilder let content: Content

    var body: some View {
        HStack(spacing: spacing) { content }
            .padding(10)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(Color.surface, in: RoundedRectangle(cornerRadius: 12))
    }
}
