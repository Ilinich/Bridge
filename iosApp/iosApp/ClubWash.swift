import SwiftUI

/// The animated club-blue wash, drawn by the Metal port of the shared shader.
///
/// The clock is passed in rather than taken here: a grid of cards should animate off one timeline,
/// not one per card.
struct ClubWash: View {

    let time: Double

    var body: some View {
        Rectangle()
            .colorEffect(
                ShaderLibrary.clubBackground(
                    .float(Float(time)),
                    .floatArray([0, 0])
                )
            )
    }
}
