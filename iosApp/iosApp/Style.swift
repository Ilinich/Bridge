import SwiftUI

extension Color {
    static let ground = Color(red: 0.04, green: 0.07, blue: 0.14)
    static let panel = Color.white.opacity(0.06)
}

struct Panel<Content: View>: View {

    let title: String?
    @ViewBuilder let content: Content

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            if let title {
                Text(title).font(.caption).foregroundStyle(.secondary)
            }
            VStack(alignment: .leading, spacing: 10) { content }
                .padding(14)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(Color.panel, in: RoundedRectangle(cornerRadius: 16))
        }
    }
}

struct Fact: View {

    let label: String
    let value: String

    var body: some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(label).font(.caption2).foregroundStyle(.secondary)
            Text(value).foregroundStyle(.white)
        }
    }
}

struct Badge: View {

    let url: String?
    let code: String
    var size: CGFloat = 40

    var body: some View {
        AsyncImage(url: url.flatMap(URL.init(string:))) { image in
            image.resizable().scaledToFit()
        } placeholder: {
            Text(code).font(.caption2).foregroundStyle(.secondary)
        }
        .frame(width: size, height: size)
    }
}

struct ScreenBackground: ViewModifier {
    func body(content: Content) -> some View {
        content
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(Color.ground)
    }
}

extension View {
    func screenBackground() -> some View { modifier(ScreenBackground()) }
}

struct Row: View {

    let leading: String
    let title: String
    let trailing: String?

    var body: some View {
        HStack {
            if !leading.isEmpty {
                Text(leading).font(.caption).foregroundStyle(.blue)
            }
            Text(title).foregroundStyle(.white)
            Spacer()
            if let trailing {
                Text(trailing).font(.system(.body, design: .monospaced)).foregroundStyle(.white)
            }
        }
    }
}
