import SwiftUI

/// The floating tab capsule, icon-only, frosted over whatever scrolls beneath it.
///
/// Same shape and the same decision as the Compose bar: the label is gone from the screen but not
/// from the tab — it stays the accessible name, so VoiceOver still announces where this goes.
struct TabBar: View {

    @Binding var selection: Tab

    private let items: [(tab: Tab, glyph: BridgeGlyph, name: String)] = [
        (.matchday, .matchday, "Matchday"),
        (.season, .season, "Season"),
        (.squad, .squad, "Squad"),
        (.club, .club, "Club"),
    ]

    var body: some View {
        HStack(spacing: 3) {
            ForEach(items, id: \.tab) { item in
                Button {
                    selection = item.tab
                    UIImpactFeedbackGenerator(style: .light).impactOccurred()
                } label: {
                    BridgeGlyphView(
                        glyph: item.glyph,
                        tint: selection == item.tab ? Color.clubBright : Color.textMuted
                    )
                        .frame(maxWidth: .infinity)
                        .frame(height: 38)
                        .contentShape(Capsule())
                }
                .accessibilityLabel(item.name)
            }
        }
        .padding(4)
        // Bars sit over moving content, so they carry far less tint than a panel does — the same
        // reasoning, and the same 26%, as the Compose bar.
        .background(Color.ground.opacity(0.26), in: Capsule())
        .background(.ultraThinMaterial, in: Capsule())
        .overlay(Capsule().strokeBorder(Color.white.opacity(0.16), lineWidth: 1))
        .environment(\.colorScheme, .dark)
        .padding(.horizontal, 18)
        .padding(.bottom, 12)
        .animation(.easeInOut(duration: 0.18), value: selection)
    }
}
