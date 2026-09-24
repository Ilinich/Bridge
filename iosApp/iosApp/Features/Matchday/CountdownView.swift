import SwiftUI
import Shared

/// The clock ticks on the Swift side; `remaining` is the shared code saying what a remaining
/// second means. A closure rather than the component itself, so the view draws in a preview too.
struct Countdown: View {

    let labels: ImplMatchdayLabels
    let remaining: (Int64) -> ImplCountdown

    var body: some View {
        TimelineView(.periodic(from: .now, by: 1)) { context in
            let left = remaining(Int64(context.date.timeIntervalSince1970 * 1000))
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
