import SwiftUI

struct Badge: View {

    let url: String?
    let code: String
    var size: CGFloat = 36

    var body: some View {
        AsyncImage(url: url.flatMap(URL.init(string:))) { image in
            image.resizable().scaledToFit()
        } placeholder: {
            Text(code)
                .font(.caption)
                .foregroundStyle(Color.textMuted)
                .frame(width: size, height: size)
                .background(Color.club.opacity(0.45), in: Circle())
        }
        .frame(width: size, height: size)
    }
}

#Preview("Placeholder and image") {
    HStack(spacing: 12) {
        Badge(url: nil, code: "CHE")
        Badge(url: nil, code: "ARS", size: 22)
        Badge(url: nil, code: "CHE", size: 68)
    }
    .padding(24)
    .background(Color.ground)
}
