import SwiftUI

/// The floating tab capsule, icon-only, frosted over whatever scrolls beneath it.
///
/// Same shape and the same decision as the Compose bar: the label is gone from the screen but not
/// from the tab — it stays the accessible name, so VoiceOver still announces where this goes.
struct TabBar: View {

    @Binding var selection: Tab

    private let items: [(tab: Tab, icon: String, name: String)] = [
        (.matchday, "shield.fill", "Matchday"),
        (.season, "calendar", "Season"),
        (.squad, "person.2.fill", "Squad"),
        (.club, "flag.fill", "Club"),
    ]

    var body: some View {
        HStack(spacing: 3) {
            ForEach(items, id: \.tab) { item in
                Button {
                    selection = item.tab
                    UIImpactFeedbackGenerator(style: .light).impactOccurred()
                } label: {
                    Image(systemName: item.icon)
                        .font(.system(size: 19, weight: .semibold))
                        .foregroundStyle(selection == item.tab ? Color.clubBright : Color.textMuted)
                        .frame(maxWidth: .infinity)
                        .frame(height: 46)
                        .contentShape(Capsule())
                }
                .accessibilityLabel(item.name)
            }
        }
        .padding(4)
        .background(.ultraThinMaterial, in: Capsule())
        .overlay(Capsule().strokeBorder(Color.white.opacity(0.16), lineWidth: 1))
        .environment(\.colorScheme, .dark)
        .padding(.horizontal, 16)
        .animation(.easeInOut(duration: 0.18), value: selection)
    }
}
