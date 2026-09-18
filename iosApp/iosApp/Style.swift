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
