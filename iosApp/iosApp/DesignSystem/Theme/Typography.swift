import SwiftUI

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
