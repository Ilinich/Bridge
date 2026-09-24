import SwiftUI
import Shared

struct PlayerCard: View {

    let player: ImplPlayerCardUi
    let time: Double

    var body: some View {
        ZStack {
            // The same wash the Compose card carries, from the same shader.
            ClubWash(time: time)

            if let number = player.shirtNumber {
                Text(number)
                    .font(.shirtNumber)
                    .foregroundStyle(Color.textPrimary.opacity(0.12))
                    .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topTrailing)
                    .padding(.horizontal, 8)
            }

            Cutout(url: player.cutoutUrl)
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .padding(.top, 10)
                .padding(.bottom, 46)

            if player.followed {
                Image(systemName: "star.fill")
                    .font(.glyphSmall)
                    .foregroundStyle(Color.clubBright)
                    .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
                    .padding(8)
            }

            VStack(alignment: .leading, spacing: 2) {
                Text(player.name)
                    .font(.label)
                    .foregroundStyle(Color.textPrimary)
                    .lineLimit(1)
                if let position = player.position {
                    Text(position).labelStyle().lineLimit(1)
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .bottomLeading)
            .padding(8)
        }
        .frame(height: 168)
        .clipShape(RoundedRectangle(cornerRadius: 13))
    }
}
