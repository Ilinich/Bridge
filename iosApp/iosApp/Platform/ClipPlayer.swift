import AVKit

/// The club screen's video, held rather than rebuilt.
///
/// Muted and paused on purpose: a clip the user is meant to drive should be waiting for them, the
/// same decision the Compose screen makes.
@Observable
final class ClipPlayer {

    let avPlayer = AVPlayer()

    private var loaded: String?

    func load(_ url: String) {
        guard loaded != url, let source = URL(string: url) else { return }
        loaded = url
        avPlayer.replaceCurrentItem(with: AVPlayerItem(url: source))
        avPlayer.isMuted = true
    }
}
