import SwiftUI
import Shared

/// How far the cutout slides against the page, and how much it fades on the way out — the same
/// numbers the Compose pager uses.
private let CutoutParallax: CGFloat = 40
private let PageFadeStrength: CGFloat = 0.4

struct PlayerPage: View {

    let player: ImplPlayerPageUi
    let labels: ImplPlayerLabels
    let width: CGFloat
    let onFollow: (String) -> Void

    var body: some View {
        // The page's own offset drives the parallax, read where it is drawn rather than kept in
        // state: the cutout slides and fades while the panel under it stays put.
        GeometryReader { page in
            let fraction = width == 0 ? 0 : -page.frame(in: .scrollView).minX / width

            ZStack(alignment: .bottom) {
                AsyncImage(url: player.cutoutUrl.flatMap(URL.init(string:))) { image in
                    image.resizable().scaledToFit()
                } placeholder: {
                    Color.clear
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .padding(.top, 70)
                .padding(.bottom, 190)
                .offset(x: fraction * CutoutParallax)
                .opacity(1 - min(abs(fraction), 1) * PageFadeStrength)

                GlassPanel {
                    PlayerFacts(player: player, labels: labels, onFollow: onFollow)
                }
                .padding(.horizontal, 14)
                .padding(.bottom, 68)
            }
        }
    }
}

struct PlayerFacts: View {

    let player: ImplPlayerPageUi
    let labels: ImplPlayerLabels
    let onFollow: (String) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack(alignment: .firstTextBaseline) {
                Text(player.name)
                    .font(.system(size: 22, weight: .heavy))
                    .foregroundStyle(Color.textPrimary)
                Spacer(minLength: 8)
                Button { onFollow(player.id) } label: {
                    Image(systemName: player.followed ? "star.fill" : "star")
                        .font(.system(size: 17))
                        .foregroundStyle(player.followed ? Color.clubBright : Color.textMuted)
                }
                .buttonStyle(.plain)
            }
            HStack(alignment: .top, spacing: 6) {
                if let number = player.shirtNumber {
                    Fact(label: labels.number.localized(), value: number)
                        .frame(maxWidth: .infinity, alignment: .leading)
                }
                if let position = player.position {
                    Fact(label: labels.position.localized(), value: position)
                        .frame(maxWidth: .infinity, alignment: .leading)
                }
                if let nationality = player.nationality {
                    Fact(label: labels.country.localized(), value: nationality)
                        .frame(maxWidth: .infinity, alignment: .leading)
                }
                if let height = player.height {
                    Fact(label: labels.height.localized(), value: height)
                        .frame(maxWidth: .infinity, alignment: .leading)
                }
            }
        }
    }
}

/// The pager's dots: five points wide when idle, a fourteen-point capsule for the page in view.
