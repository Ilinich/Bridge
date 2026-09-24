import SwiftUI

/// A player's cut-out photograph, and what stands in for one.
///
/// The stand-in is not `Color.clear`: a card whose photograph has not arrived was an empty
/// rectangle, which is what the squad looks like on a slow connection and not only in a preview.
/// A silhouette says the same thing a missing badge says with its three letters — there is a
/// person here, the picture is not in yet.
struct Cutout: View {

    let url: String?

    @Environment(\.usesBundledImages) private var usesBundledImages

    var body: some View {
        if let url, usesBundledImages {
            Image(bundled(for: url)).resizable().scaledToFit()
        } else {
            AsyncImage(url: url.flatMap(URL.init(string:))) { image in
                image.resizable().scaledToFit()
            } placeholder: {
                Silhouette()
            }
        }
    }

    /// Two bundled photographs rather than one, chosen by the url that asked: a grid where every
    /// card is the same face reads as a mistake in the layout rather than as stand-in data.
    private func bundled(for url: String) -> String {
        let checksum = url.utf8.reduce(into: UInt8(0)) { total, byte in total = total &+ byte }
        return checksum.isMultiple(of: 2) ? "preview-cutout" : "preview-cutout-alt"
    }
}

private struct Silhouette: View {

    var body: some View {
        Image(systemName: "person.fill")
            .resizable()
            .scaledToFit()
            .padding(.horizontal, 24)
            .foregroundStyle(Color.textPrimary.opacity(0.10))
    }
}

#Preview("Waiting for the photograph") {
    Cutout(url: nil)
        .frame(height: 200)
        .padding(24)
        .background(Color.ground)
}
