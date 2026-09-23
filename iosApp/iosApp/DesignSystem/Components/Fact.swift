import SwiftUI

struct Fact: View {

    let label: String
    let value: String

    var body: some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(label).labelStyle()
            Text(value)
                .font(.label)
                .foregroundStyle(Color.textPrimary)
                .lineLimit(1)
        }
    }
}

#Preview {
    HStack(alignment: .top, spacing: 6) {
        Fact(label: "ARENA", value: "Stamford Bridge")
            .frame(maxWidth: .infinity, alignment: .leading)
        Fact(label: "CAPACITY", value: "40,343")
            .frame(maxWidth: .infinity, alignment: .leading)
    }
    .padding(24)
    .background(Color.ground)
}
