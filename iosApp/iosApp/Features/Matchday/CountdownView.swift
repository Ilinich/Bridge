import SwiftUI
import Shared

struct Countdown: View {

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

    struct Cell: View {
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
