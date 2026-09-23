import SwiftUI

/// The three states a screen can be in, in one place — the counterpart of the Compose
/// `LoadableContent`, and the same order of questions: a failure is reported before a load, and
/// content only when there is content.
///
/// Without this every screen showed an empty dark page when the network was gone: the state had
/// `error` and `retry()` all along and nothing read them.
extension View {

    /// Shows this view only when there is something to show, and the loading or failure state
    /// otherwise. A modifier rather than a container so a screen's body stays a flat list of
    /// sections instead of gaining a level of nesting for a question asked once at the top.
    @ViewBuilder
    func loadable(
        isLoading: Bool,
        hasFailed: Bool,
        onRetry: @escaping () -> Void
    ) -> some View {
        switch LoadableState(isLoading: isLoading, hasFailed: hasFailed) {
        case .failed: RetryPrompt(onRetry: onRetry)
        case .loading: LoadingIndicator()
        case .content: self
        }
    }
}

/// Which of the three a screen is in. A separate type because it is the part worth a test: the
/// order of the questions is the behaviour, and it is easy to reverse by accident.
enum LoadableState: Equatable {

    case loading
    case failed
    case content

    init(isLoading: Bool, hasFailed: Bool) {
        if hasFailed {
            self = .failed
        } else if isLoading {
            self = .loading
        } else {
            self = .content
        }
    }
}

private struct LoadingIndicator: View {

    var body: some View {
        ProgressView()
            .tint(Color.textMuted)
            .frame(maxWidth: .infinity)
            .padding(.top, 120)
    }
}

private struct RetryPrompt: View {

    let onRetry: () -> Void

    var body: some View {
        VStack(spacing: 10) {
            Text("loadable.error.title")
                .font(.subheading)
                .foregroundStyle(Color.textPrimary)
            Text("loadable.error.body")
                .font(.prose)
                .foregroundStyle(Color.textMuted)
                .multilineTextAlignment(.center)
            Button(action: onRetry) {
                Text("loadable.retry")
                    .font(.label)
                    .foregroundStyle(Color.clubBright)
                    .padding(.horizontal, 18)
                    .padding(.vertical, 10)
                    .background(Color.surface, in: Capsule())
            }
            .buttonStyle(.plain)
            .padding(.top, 4)
        }
        .frame(maxWidth: .infinity)
        .padding(.top, 100)
    }
}

/// What a screen says when it is showing what it had rather than what it has.
struct OfflineNotice: View {

    var body: some View {
        Text("offline.notice")
            .labelStyle()
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.bottom, 2)
    }
}

#Preview("States") {
    VStack(spacing: 40) {
        Color.clear.frame(height: 0).loadable(isLoading: true, hasFailed: false, onRetry: {})
        Color.clear.frame(height: 0).loadable(isLoading: false, hasFailed: true, onRetry: {})
        OfflineNotice()
    }
    .padding(.horizontal, 14)
    .frame(maxHeight: .infinity)
    .background(Color.ground)
}
