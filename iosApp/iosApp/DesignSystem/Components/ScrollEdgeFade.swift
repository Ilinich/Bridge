import SwiftUI

/// A dark band at the end of the scroll, on an eased ramp.
///
/// It dims what runs under the bar and nothing more — the same job, and the same 0.88 peak, as the
/// Compose fade. The ramp is many-stopped because a short gradient on an 8-bit ramp holds one
/// value long enough to read as a step.
struct ScrollEdgeFade: View {

    enum Edge { case top, bottom }

    var edge: Edge = .bottom
    var height: CGFloat = 110

    var body: some View {
        LinearGradient(
            stops: (0..<12).map { index in
                let t = Double(index) / 11
                let distanceFromEdge = edge == .top ? t : 1 - t
                let ramp = 1 - distanceFromEdge
                let eased = ramp * ramp * (3 - 2 * ramp)
                return .init(color: Color.ground.opacity(0.88 * eased), location: t)
            },
            startPoint: .top,
            endPoint: .bottom
        )
        .frame(height: height)
        .allowsHitTesting(false)
        .ignoresSafeArea(edges: edge == .top ? .top : .bottom)
    }
}
