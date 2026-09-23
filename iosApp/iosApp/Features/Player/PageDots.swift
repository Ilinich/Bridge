import SwiftUI
import Shared

struct PageDots: View {

    let count: Int
    let selected: Int

    var body: some View {
        HStack(spacing: 5) {
            ForEach(0..<max(count, 0), id: \.self) { index in
                Capsule()
                    .fill(index == selected ? Color.clubBright : Color.line)
                    .frame(width: index == selected ? 14 : 5, height: 5)
            }
        }
        .animation(.easeInOut(duration: 0.2), value: selected)
    }
}
