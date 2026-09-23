import SwiftUI
import Shared

/// The floating tab capsule, icon-only, frosted over whatever scrolls beneath it.
///
/// Same shape and the same decision as the Compose bar: the label is gone from the screen but not
/// from the tab — it stays the accessible name, so VoiceOver still announces where this goes.
struct TabBar: View {

    @Binding var selection: Tab

    /// The names come from the shared bundle, not from a literal here: the Compose bar reads the
    /// same four keys, so a tab is translated once for both apps.
    private let items: [Item] = [
        Item(tab: .matchday, glyph: .matchday, name: IosStrings.shared.tabMatchday),
        Item(tab: .season, glyph: .season, name: IosStrings.shared.tabSeason),
        Item(tab: .squad, glyph: .squad, name: IosStrings.shared.tabSquad),
        Item(tab: .club, glyph: .club, name: IosStrings.shared.tabClub)
    ]

    private struct Item: Identifiable {
        let tab: Tab
        let glyph: BridgeGlyph
        let name: any ResourcesStringDesc

        var id: Tab { tab }
    }

    var body: some View {
        HStack(spacing: 3) {
            ForEach(items) { item in
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
                .accessibilityLabel(item.name.localized())
            }
        }
        .padding(4)
        // Bars sit over moving content, so they carry far less tint than a panel does — the same
        // reasoning as the Compose bar. Less than its 26% here, because the material already
        // darkens what it blurs and the two together hid the blur altogether.
        .background(Color.ground.opacity(0.14), in: Capsule())
        .background(.ultraThinMaterial, in: Capsule())
        .overlay(Capsule().strokeBorder(Color.white.opacity(0.16), lineWidth: 1))
        .environment(\.colorScheme, .dark)
        // The Compose bar has no shadow of its own: what reads as one is the dark band the scroll
        // fades into behind it. A floating capsule needs the separation either way.
        .shadow(color: Color.black.opacity(0.45), radius: 16, y: 6)
        .padding(.horizontal, 18)
        // The page already ends above the home indicator, so the bar only needs the inset the
        // Compose bar has over its own navigation-bar padding.
        .padding(.bottom, 12)
        .animation(.easeInOut(duration: 0.18), value: selection)
    }
}
