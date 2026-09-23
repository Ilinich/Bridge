import SwiftUI
import Shared

struct HeroCard: View {

    let component: ImplMatchdayComponent
    let state: ImplMatchdayUiState

    var body: some View {
        GlassPanel {
            HStack(alignment: .top, spacing: 12) {
                Text(state.nextMatch?.competition ?? state.labels.nextMatch.localized())
                    .labelStyle()
                Spacer(minLength: 0)
                if let venue = state.nextMatch?.venue {
                    Text(venue).labelStyle().multilineTextAlignment(.trailing)
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)

            if let match = state.nextMatch {
                Versus(match: match, versus: state.labels.versus.localized())
                VStack(spacing: 3) {
                    Text(match.kickoffText)
                        .font(.titleSmall)
                        .foregroundStyle(Color.textPrimary)
                    Text(state.labels.kickoffLocal.localized()).labelStyle()
                }
                Countdown(
                    component: component,
                    kickoffMillis: match.kickoffMillis,
                    labels: state.labels
                )
            } else {
                Text(fallback)
                    .font(.labelLarge)
                    .foregroundStyle(Color.textMuted)
                    .frame(maxWidth: .infinity)
            }
        }
    }

    private var fallback: String {
        if state.nextMatchFailed { return state.labels.fixtureFailed.localized() }
        if state.nextMatchLoaded { return state.labels.noFixture.localized() }
        return state.labels.loadingFixture.localized()
    }
}

struct Versus: View {

    let match: ImplNextMatchUi
    let versus: String

    var body: some View {
        HStack(spacing: 12) {
            Side(name: match.homeName, code: match.homeCode, badge: match.homeBadgeUrl)
            Text(versus).font(.figure).foregroundStyle(Color.textMuted)
            Side(name: match.awayName, code: match.awayCode, badge: match.awayBadgeUrl)
        }
        .frame(maxWidth: .infinity)
    }

    struct Side: View {
        let name: String
        let code: String
        let badge: String?

        var body: some View {
            VStack(spacing: 6) {
                Badge(url: badge, code: code, size: 36)
                Text(name)
                    .font(.labelLarge)
                    .foregroundStyle(Color.textPrimary)
                    .multilineTextAlignment(.center)
            }
            .frame(width: 84)
        }
    }
}

/// The clock ticks on the Swift side; the shared code only says what a remaining second means.
