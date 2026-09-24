import Shared
import Testing

@testable import Bridge

/// The preview mocks are ordinary Kotlin values, and a preview that fails to build one is found
/// only by opening the canvas. These construct every state a screen preview draws and read the
/// parts it reads — including the words, which resolve here without a graph behind them.
struct PreviewStatesTests {

    @Test func every_screen_has_a_state_to_draw() {
        #expect(IosPreviews.shared.matchday().nextMatch != nil)
        #expect(!IosPreviews.shared.season().rounds.isEmpty)
        #expect(!IosPreviews.shared.squad().players.isEmpty)
        #expect(!IosPreviews.shared.player().players.isEmpty)
        #expect(IosPreviews.shared.club().club != nil)
        #expect(IosPreviews.shared.matchDetail().match != nil)
    }

    @Test func the_empty_states_are_the_ones_the_screens_branch_on() {
        #expect(IosPreviews.shared.squadLoading().isLoading)
        #expect(IosPreviews.shared.squadFailed().error != nil)
        #expect(IosPreviews.shared.matchdayEmpty().nextMatch == nil)
        #expect(IosPreviews.shared.matchdayEmpty().isOffline)
    }

    @Test func the_words_resolve() {
        #expect(!IosPreviews.shared.matchday().labels.recent.localized().isEmpty)
        #expect(IosPreviews.shared.matchDetail().match?.scoreline.localized() == "2 : 1")
    }
}
