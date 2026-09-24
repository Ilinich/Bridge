import AVKit
import SwiftUI

/// The club's video.
///
/// A preview draws a still instead: `VideoPlayer` renders nothing in a canvas, and a black
/// rectangle where the media section should be says nothing about the layout around it.
struct Clip: View {

    let url: String
    let onStarted: () -> Void

    @State private var player = ClipPlayer()
    @Environment(\.usesBundledImages) private var usesBundledImages

    var body: some View {
        Group {
            if usesBundledImages {
                Poster()
            } else {
                VideoPlayer(player: player.avPlayer)
                    .onAppear {
                        player.load(url)
                        onStarted()
                    }
            }
        }
        .frame(height: 180)
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}

private struct Poster: View {

    var body: some View {
        ZStack {
            Image("preview-backdrop").resizable().scaledToFill()
            Color.ground.opacity(0.35)
            Image(systemName: "play.circle.fill")
                .font(.system(size: 44))
                .foregroundStyle(Color.textPrimary.opacity(0.85))
        }
    }
}
