import SwiftUI

/// The frosted panel: Haze there, the system material here — the one place the platforms differ.
struct GlassPanel<Content: View>: View {

    @ViewBuilder let content: Content

    var body: some View {
        VStack(spacing: 11) { content }
            .padding(14)
            .frame(maxWidth: .infinity)
            // Haze tints the blur with the ground at 55%; the material alone reads far lighter
            // than the Compose panel, so the same tint is painted over it.
            .background(Color.ground.opacity(0.55), in: RoundedRectangle(cornerRadius: 16))
            .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: 16))
            .overlay(
                RoundedRectangle(cornerRadius: 16)
                    .strokeBorder(Color.white.opacity(0.14), lineWidth: 1)
            )
            .environment(\.colorScheme, .dark)
    }
}

#Preview {
    ZStack {
        ClubWash()
        GlassPanel {
            Text("Stamford Bridge").font(.display).foregroundStyle(Color.textPrimary)
            Text("The Blues").labelStyle()
        }
        .padding(14)
    }
    .background(Color.ground)
}
