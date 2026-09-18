import SwiftUI
import Shared

struct MatchdayScreen: View {


    @StateObject private var model = ScreenModel(
        component: IosBridge.shared.matchday(),
        state: { $0.state },
        close: { $0.close() }
    )

    var body: some View {
        let state = model.state
        Screen(backdropUrl: state.backdropUrl) {
            HeroCard(component: model.component, state: state)

            if !state.following.isEmpty {
                Section(title: state.labels.following.localized()) {
                    VStack(spacing: 6) {
                        ForEach(state.following, id: \.id) { player in
                            Button {
                                model.component.viewModel.onFollowedPlayerClick(playerId: player.id)
                            } label: {
                                SurfaceRow {
                                    Image(systemName: "star.fill")
                                        .font(.system(size: 13))
                                        .foregroundStyle(Color.clubBright)
                                    Text(player.name)
                                        .font(.labelLarge)
                                        .foregroundStyle(Color.textPrimary)
                                        .lineLimit(1)
                                    Spacer(minLength: 0)
                                }
                            }
                        }
                    }
                }
            }

            if let recent = state.recent {
                Section(title: state.labels.recent.localized()) {
                    SurfaceRow {
                        Badge(url: recent.awayBadgeUrl, code: recent.awayCode, size: 22)
                        VStack(alignment: .leading, spacing: 2) {
                            Text(recent.teams.localized())
                                .font(.labelLarge)
                                .foregroundStyle(Color.textPrimary)
                                .lineLimit(1)
                            Text(recent.competition).labelStyle()
                        }
                        Spacer(minLength: 8)
                        if let score = recent.score {
                            Text(score.localized())
                                .font(.figure)
                                .foregroundStyle(Color.textPrimary)
                        }
                    }
                }
            }

            if let stadium = state.stadium {
                Section(title: state.labels.stadium.localized()) {
                    Button {
                        model.component.viewModel.onStadiumClick()
                    } label: {
                        SurfaceRow(spacing: 6) {
                            // Equal columns. The Compose row weights them 1.7 / 1 / 1, and the
                            // obvious SwiftUI translation — containerRelativeFrame — asks the
                            // scroll container for its width while the container is asking this
                            // row for its height, and the layout pass never terminates.
                            Fact(label: state.labels.arena.localized(), value: stadium.arena)
                                .frame(maxWidth: .infinity, alignment: .leading)
                            Fact(label: state.labels.capacity.localized(), value: stadium.capacity)
                                .frame(maxWidth: .infinity, alignment: .leading)
                            Fact(label: state.labels.founded.localized(), value: stadium.founded)
                                .frame(maxWidth: .infinity, alignment: .leading)
                        }
                    }
                    .buttonStyle(.plain)
                }
            }

            if state.isOffline {
                Text("Offline").labelStyle()
            }
        }
    }
}

private struct HeroCard: View {

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

private struct Versus: View {

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

    private struct Side: View {
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
private struct Countdown: View {

    let component: ImplMatchdayComponent
    let kickoffMillis: Int64
    let labels: ImplMatchdayLabels

    var body: some View {
        TimelineView(.periodic(from: .now, by: 1)) { context in
            let now = Int64(context.date.timeIntervalSince1970 * 1000)
            let left = component.countdown(nowMillis: now, kickoffMillis: kickoffMillis)
            HStack(spacing: 6) {
                Cell(value: left.days, unit: labels.days.localized())
                Cell(value: left.hours, unit: labels.hours.localized())
                Cell(value: left.minutes, unit: labels.minutes.localized())
                Cell(value: left.seconds, unit: labels.seconds.localized())
            }
        }
    }

    private struct Cell: View {
        let value: Int64
        let unit: String

        var body: some View {
            VStack(spacing: 2) {
                Text(String(format: "%02d", value))
                    .font(.figure)
                    .foregroundStyle(Color.textPrimary)
                Text(unit).labelStyle()
            }
            .frame(minWidth: 42)
            .padding(.vertical, 5)
            .padding(.horizontal, 8)
            .background(Color.club.opacity(0.42), in: RoundedRectangle(cornerRadius: 8))
        }
    }
}
