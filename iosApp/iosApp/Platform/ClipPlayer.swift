import AVKit

/// The club screen's video, held rather than rebuilt.
///
/// Muted and paused on purpose: a clip the user is meant to drive should be waiting for them, the
/// same decision the Compose screen makes.
@Observable
final class ClipPlayer {

    let avPlayer = AVPlayer()

    private var loaded: String?

    /// Loads `url` unless it is already playing.
    ///
    /// The scheme is checked rather than left to `URL(string:)`: since iOS 17 that initialiser
    /// parses leniently and hands back a relative URL for a string that is plainly not one —
    /// `"not a url"` becomes `not%20a%20url` — so it reports syntax, not usability, and the player
    /// would sit on an item that can never load.
    func load(_ url: String) {
        guard loaded != url, let source = URL(string: url), source.isHTTP else { return }
        loaded = url
        avPlayer.replaceCurrentItem(with: AVPlayerItem(url: source))
        avPlayer.isMuted = true
    }
}

private extension URL {

    var isHTTP: Bool {
        scheme == "https" || scheme == "http"
    }
}
