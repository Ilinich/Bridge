import SwiftUI
import Shared

struct ClubScreen: View {


    @StateObject private var model = ScreenModel(
        component: IosBridge.shared.club(),
        state: { $0.state },
        close: { $0.close() }
    )

    var body: some View {
        let state = model.state
        let labels = state.labels
        Screen(backdropUrl: state.club?.backdropUrl) {
                if let club = state.club {
                    SurfaceRow(spacing: 14) {
                        Badge(url: club.badgeUrl, code: club.code, size: 48)
                        VStack(alignment: .leading, spacing: 4) {
                            Text(club.name)
                                .font(.headline)
                                .foregroundStyle(Color.textPrimary)
                            if let nicknames = club.nicknames {
                                Text(nicknames).labelStyle()
                            }
                        }
                        Spacer(minLength: 8)
                        if let founded = club.founded {
                            Fact(label: labels.founded.localized(), value: founded)
                        }
                    }

                    if let summary = club.summary {
                        Section(title: labels.about.localized()) {
                            SurfaceRow {
                                Text(summary)
                                    .font(.system(size: 14))
                                    .foregroundStyle(Color.textMuted)
                                    .lineSpacing(3)
                            }
                        }
                    }

                    if !club.links.isEmpty {
                        Section(title: labels.links.localized()) {
                            VStack(spacing: 6) {
                                ForEach(club.links, id: \.url) { link in
                                    if let url = URL(string: link.url) {
                                        Link(destination: url) {
                                            SurfaceRow {
                                                Text(link.label.localized())
                                                    .font(.labelLarge)
                                                    .foregroundStyle(Color.clubBright)
                                                Spacer(minLength: 0)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if let ground = state.ground {
                    Section(title: labels.ground.localized()) {
                        VStack(alignment: .leading, spacing: 6) {
                            SurfaceRow {
                                Text(ground.name)
                                    .font(.labelLarge)
                                    .foregroundStyle(Color.textPrimary)
                                Spacer(minLength: 0)
                            }
                            SurfaceRow(spacing: 6) {
                                if let capacity = ground.capacity {
                                    Fact(label: labels.capacity.localized(), value: capacity)
                                        .frame(maxWidth: .infinity, alignment: .leading)
                                }
                                if let opened = ground.opened {
                                    Fact(label: labels.opened.localized(), value: opened)
                                        .frame(maxWidth: .infinity, alignment: .leading)
                                }
                                if let location = ground.location {
                                    Fact(label: labels.location.localized(), value: location)
                                        .frame(maxWidth: .infinity, alignment: .leading)
                                }
                            }
                        }
                    }
                }
        }
    }
}
