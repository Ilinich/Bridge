import SwiftUI

/// The Compose palette, value for value.
extension Color {
    static let club = Color(red: 0x03 / 255, green: 0x46 / 255, blue: 0x94 / 255)
    static let clubBright = Color(red: 0x3E / 255, green: 0x86 / 255, blue: 0xE8 / 255)
    static let ground = Color(red: 0x04 / 255, green: 0x10 / 255, blue: 0x1F / 255)
    static let surface = Color(red: 0x0C / 255, green: 0x1E / 255, blue: 0x33 / 255)
    static let line = Color(red: 0x16 / 255, green: 0x30 / 255, blue: 0x4C / 255)
    static let textPrimary = Color(red: 0xEA / 255, green: 0xF1 / 255, blue: 0xFA / 255)
    static let textMuted = Color(red: 0x8A / 255, green: 0xA1 / 255, blue: 0xBC / 255)
}

/// The Compose type scale, value for value: figures are monospaced, field names are monospaced
/// and tracked, everything else is the Material label/title size the original picked.
extension Font {
    static let figure = Font.system(size: 16, weight: .bold, design: .monospaced)
    static let labelMono = Font.system(size: 10, weight: .medium, design: .monospaced)
    static let labelLarge = Font.system(size: 14, weight: .medium)
    static let titleSmall = Font.system(size: 14, weight: .medium)
    static let headline = Font.system(size: 22, weight: .heavy)
}

extension Text {
    func labelStyle() -> some View {
        font(.labelMono).tracking(1.2).foregroundStyle(Color.textMuted)
    }
}

/// A full-bleed photograph with the scrim that makes text on top of it readable.
///
/// Applied as a background rather than as a sibling layer: as a layer it covered the content,
/// which is exactly the kind of mistake the Compose side made unrepresentable with GlassBackdrop.
struct Backdrop: View {

    let url: String?

    var body: some View {
        ZStack {
            Color.ground
            AsyncImage(url: url.flatMap(URL.init(string:))) { image in
                image.resizable().scaledToFill()
            } placeholder: {
                Color.clear
            }
            LinearGradient(
                stops: [
                    .init(color: .clear, location: 0),
                    .init(color: Color.ground.opacity(0.65), location: 0.45),
                    .init(color: Color.ground, location: 1),
                ],
                startPoint: .top,
                endPoint: .bottom
            )
        }
        .clipped()
        .ignoresSafeArea()
    }
}

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

struct Fact: View {

    let label: String
    let value: String

    var body: some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(label).labelStyle()
            Text(value)
                .font(.labelLarge)
                .foregroundStyle(Color.textPrimary)
                .lineLimit(1)
        }
    }
}

struct Badge: View {

    let url: String?
    let code: String
    var size: CGFloat = 36

    var body: some View {
        AsyncImage(url: url.flatMap(URL.init(string:))) { image in
            image.resizable().scaledToFit()
        } placeholder: {
            Text(code)
                .font(.labelMono)
                .foregroundStyle(Color.textMuted)
                .frame(width: size, height: size)
                .background(Color.club.opacity(0.45), in: Circle())
        }
        .frame(width: size, height: size)
    }
}

/// Screen chrome: the backdrop behind the scroll, the same insets the Compose screens use, and
/// room at the bottom for the floating bar.
struct Screen<Content: View>: View {

    var backdropUrl: String? = nil
    @ViewBuilder let content: Content

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 12) { content }
                .padding(.horizontal, 14)
                .padding(.vertical, 16)
                .padding(.bottom, 74)
        }
        .background(Backdrop(url: backdropUrl))
        .toolbarBackground(.hidden, for: .navigationBar)
    }
}
