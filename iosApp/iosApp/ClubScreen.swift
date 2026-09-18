import SwiftUI
import Shared

struct ClubScreen: View {

    @StateObject private var model: ScreenModel<ImplClubUiState>
    private let component: ImplClubComponent

    init() {
        let component = IosBridge.shared.club()
        self.component = component
        _model = StateObject(
            wrappedValue: ScreenModel(flow: component.state, close: component.close)
        )
    }

    var body: some View {
        let state = model.state
        let labels = state.labels
        ScrollView {
            VStack(alignment: .leading, spacing: 18) {
                if let club = state.club {
                    Panel(title: nil) {
                        HStack(spacing: 14) {
                            Badge(url: club.badgeUrl, code: club.code, size: 56)
                            VStack(alignment: .leading, spacing: 4) {
                                Text(club.name).font(.title3).foregroundStyle(.white)
                                if let nicknames = club.nicknames {
                                    Text(nicknames).font(.caption).foregroundStyle(.secondary)
                                }
                            }
                        }
                        HStack(spacing: 24) {
                            if let founded = club.founded {
                                Fact(label: labels.founded.localized(), value: founded)
                            }
                        }
                    }

                    if let summary = club.summary {
                        Panel(title: labels.about.localized()) {
                            Text(summary).font(.footnote).foregroundStyle(.secondary)
                        }
                    }

                    if !club.links.isEmpty {
                        Panel(title: labels.links.localized()) {
                            ForEach(club.links, id: \.url) { link in
                                Link(link.label.localized(), destination: URL(string: link.url)!)
                                    .font(.footnote)
                            }
                        }
                    }
                }

                if let ground = state.ground {
                    Panel(title: labels.ground.localized()) {
                        Text(ground.name).foregroundStyle(.white)
                        HStack(spacing: 24) {
                            if let capacity = ground.capacity {
                                Fact(label: labels.capacity.localized(), value: capacity)
                            }
                            if let opened = ground.opened {
                                Fact(label: labels.opened.localized(), value: opened)
                            }
                            if let location = ground.location {
                                Fact(label: labels.location.localized(), value: location)
                            }
                        }
                    }
                }
            }
            .padding(20)
        }
        .screenBackground()
        .navigationTitle("Club")
    }
}
