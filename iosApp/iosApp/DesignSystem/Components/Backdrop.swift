import SwiftUI

/// A full-bleed photograph with the scrim that makes text on top of it readable.
///
/// Applied as a background rather than as a sibling layer: as a layer it covered the content,
/// which is exactly the kind of mistake the Compose side made unrepresentable with GlassBackdrop.
struct Backdrop: View {

    let url: String?

    @Environment(\.usesBundledImages) private var usesBundledImages

    var body: some View {
        ZStack {
            Color.ground
            if url != nil, usesBundledImages {
                Image("preview-backdrop").resizable().scaledToFill()
            } else {
                AsyncImage(url: url.flatMap(URL.init(string:))) { image in
                    image.resizable().scaledToFill()
                } placeholder: {
                    Color.clear
                }
            }
            LinearGradient(
                stops: [
                    .init(color: .clear, location: 0),
                    .init(color: Color.ground.opacity(0.65), location: 0.45),
                    .init(color: Color.ground, location: 1)
                ],
                startPoint: .top,
                endPoint: .bottom
            )
        }
        .clipped()
        .ignoresSafeArea()
    }
}
