import SwiftUI

/// The type scale, carrying the same values as the Compose original.
///
/// Roles, not sizes: a view asks for the part a piece of text plays and never for a number, so the
/// scale stays the one place a size is decided. Names avoid the system roles (`title`, `body`,
/// `headline`) on purpose — a token that shadows `Font.body` reads like the system one at the call
/// site and is not.
extension Font {

    /// The club name — the largest type in the app.
    static let display = Font.system(size: 28, weight: .heavy)

    /// A player's name over the glass panel.
    static let heading = Font.system(size: 22, weight: .heavy)

    /// A team's name beside a scoreline.
    static let subheading = Font.system(size: 17, weight: .semibold)

    /// Scorelines and countdowns. Monospaced so a changing digit never reflows the row.
    static let figure = Font.system(size: 16, weight: .bold, design: .monospaced)

    /// The shirt number watermarked across a squad card.
    static let shirtNumber = Font.system(size: 32, weight: .bold, design: .monospaced)

    /// Names, values, anything a row is about.
    static let label = Font.system(size: 14, weight: .medium)

    /// Running text: a summary, an explanation.
    static let prose = Font.system(size: 14)

    /// Field names and other chrome, set apart by being monospaced and tracked.
    static let caption = Font.system(size: 10, weight: .medium)

    /// Symbols pulled to text size: `glyph` sits beside `subheading`, `glyphSmall` beside `label`.
    static let glyph = Font.system(size: 17)
    static let glyphSmall = Font.system(size: 13)
}

extension Text {

    /// The caption treatment, applied as one decision: nothing sets tracking on its own.
    func labelStyle() -> some View {
        font(.caption).tracking(1.2).foregroundStyle(Color.textMuted)
    }
}

#Preview("Type scale") {
    VStack(alignment: .leading, spacing: 12) {
        Text("Display").font(.display)
        Text("Heading").font(.heading)
        Text("Subheading").font(.subheading)
        Text("12:34:56").font(.figure)
        Text("Label").font(.label)
        Text("Running prose over a couple of lines.").font(.prose)
        Text("CAPTION").labelStyle()
    }
    .foregroundStyle(Color.textPrimary)
    .frame(maxWidth: .infinity, alignment: .leading)
    .padding(24)
    .background(Color.ground)
}
