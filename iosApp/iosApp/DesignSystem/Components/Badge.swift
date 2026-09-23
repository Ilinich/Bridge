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
                .font(.labelMono)
                .foregroundStyle(Color.textMuted)
                .frame(width: size, height: size)
                .background(Color.club.opacity(0.45), in: Circle())
        }
        .frame(width: size, height: size)
    }
}
