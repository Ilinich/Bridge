import SwiftUI

/// The three states a screen can be in, in one place — the counterpart of the Compose
/// `LoadableContent`, and the same order of questions: a failure is reported before a load, and
/// content only when there is content.
///
/// Without this every screen showed an empty dark page when the network was gone: the state had
/// `error` and `retry()` all along and nothing read them.
struct LoadableView<Content: View>: View {

    let isLoading: Bool
    let hasFailed: Bool
    let onRetry: () -> Void
    @ViewBuilder let content: Content

    var body: some View {
        if hasFailed {
            Failure(onRetry: onRetry)
        } else if isLoading {
            Loading()
        } else {
            content
        }
    }
}

private struct Loading: View {

    var body: some View {
        ProgressView()
            .tint(Color.textMuted)
            .frame(maxWidth: .infinity)
            .padding(.top, 120)
    }
}

private struct Failure: View {

    let onRetry: () -> Void

    var body: some View {
        VStack(spacing: 10) {
            Text("loadable.error.title")
                .font(.system(size: 17, weight: .semibold))
                .foregroundStyle(Color.textPrimary)
            Text("loadable.error.body")
                .font(.system(size: 14))
                .foregroundStyle(Color.textMuted)
                .multilineTextAlignment(.center)
            Button(action: onRetry) {
                Text("loadable.retry")
                    .font(.labelLarge)
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
