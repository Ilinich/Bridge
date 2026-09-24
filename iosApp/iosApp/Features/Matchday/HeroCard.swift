import SwiftUI
import Shared

struct HeroCard: View {

    let state: ImplMatchdayUiState
    let remaining: (Int64) -> ImplCountdown

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
                        .font(.label)
                        .foregroundStyle(Color.textPrimary)
                    Text(state.labels.kickoffLocal.localized()).labelStyle()
                }
                Countdown(labels: state.labels, remaining: remaining)
            } else {
                Text(fallback)
                    .font(.label)
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
                    .font(.label)
                    .foregroundStyle(Color.textPrimary)
                    .multilineTextAlignment(.center)
            }
            .frame(width: 84)
        }
    }
}
