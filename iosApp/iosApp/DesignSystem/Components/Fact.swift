import SwiftUI

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
