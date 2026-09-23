import Testing

@testable import Bridge

/// The club screen's clip is held rather than rebuilt, and holding it is only worth anything if
/// the same url does not replace the item that is already playing it.
@MainActor
struct ClipPlayerTests {

    @Test func the_same_url_is_loaded_once() {
        let player = ClipPlayer()
        player.load("https://example.com/clip.mp4")
        let item = player.avPlayer.currentItem

        player.load("https://example.com/clip.mp4")

        #expect(player.avPlayer.currentItem === item)
    }

    @Test func a_new_url_replaces_the_item() {
        let player = ClipPlayer()
        player.load("https://example.com/clip.mp4")
        let item = player.avPlayer.currentItem

        player.load("https://example.com/other.mp4")

        #expect(player.avPlayer.currentItem !== item)
    }

    @Test func a_string_that_is_not_a_url_is_ignored() {
        let player = ClipPlayer()

        player.load("not a url")

        #expect(player.avPlayer.currentItem == nil)
    }
}
