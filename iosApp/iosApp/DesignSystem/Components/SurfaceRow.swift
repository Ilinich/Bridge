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

#Preview {
    VStack(spacing: 6) {
        SurfaceRow {
            Text("Cole Palmer")
                .font(.label)
                .foregroundStyle(Color.textPrimary)
            Spacer(minLength: 0)
        }
        SurfaceRow {
            Badge(url: nil, code: "ARS", size: 22)
            Text("Chelsea v Arsenal")
                .font(.label)
                .foregroundStyle(Color.textPrimary)
            Spacer(minLength: 8)
            Text("2 - 1").font(.figure).foregroundStyle(Color.textPrimary)
        }
    }
    .padding(14)
    .background(Color.ground)
}
