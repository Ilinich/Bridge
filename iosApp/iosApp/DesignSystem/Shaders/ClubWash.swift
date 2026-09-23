import SwiftUI

/// The club-blue wash, drawn by the Metal port of the shared shader.
///
/// The Compose app writes this shader once in the dialect AGSL and SkSL share and runs it on both
/// platforms. A SwiftUI app has neither runtime, so the same maths is a third file in a third
/// language — and because it is the same maths with the same constants, the two backgrounds move
/// in the same way rather than merely looking similar.
struct ClubWash: View {

    /// A clock to share. A grid of cards runs off one timeline; a screen-sized wash keeps its own.
    var time: Double?

    @Environment(\.isOnScreen) private var isOnScreen

    var body: some View {
        if let time {
            wash(at: time)
        } else if isOnScreen {
            TimelineView(.animation) { context in
                wash(at: context.date.timeIntervalSinceReferenceDate)
            }
            .ignoresSafeArea()
        } else {
            // A tab that is alive but not in view keeps its state, not its frame rate.
            wash(at: 0).ignoresSafeArea()
        }
    }

    private func wash(at time: Double) -> some View {
        GeometryReader { geometry in
            Rectangle()
                .colorEffect(
                    ShaderLibrary.clubBackground(
                        // The shader's own clock wraps every hour: a float loses the resolution to
                        // move smoothly once the number gets large.
                        .float(Float(time.truncatingRemainder(dividingBy: 3600))),
                        .float2(geometry.size.width, geometry.size.height)
                    )
                )
        }
    }
}
