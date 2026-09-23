import Testing

@testable import Bridge

/// The order in which a screen asks its three questions. Reversing it is a one-word edit and shows
/// up only as a spinner on a screen that has already failed.
struct LoadableStateTests {

    @Test func a_failure_is_reported_before_a_load() {
        #expect(LoadableState(isLoading: true, hasFailed: true) == .failed)
    }

    @Test func content_comes_only_when_there_is_content() {
        #expect(LoadableState(isLoading: true, hasFailed: false) == .loading)
        #expect(LoadableState(isLoading: false, hasFailed: false) == .content)
    }
}
