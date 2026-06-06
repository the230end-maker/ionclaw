import SwiftUI

// run-state indicator: a tinted halo around a solid dot
struct ServerStatusView: View {
    let isRunning: Bool

    private var dotColor: Color {
        isRunning ? Theme.running : Color(hex: 0xBDBDBD)
    }

    var body: some View {
        VStack(spacing: 12) {
            ZStack {
                Circle()
                    .fill(dotColor.opacity(0.12))
                    .frame(width: 56, height: 56)

                Circle()
                    .fill(dotColor)
                    .frame(width: 20, height: 20)
            }

            Text(isRunning ? "Running" : "Stopped")
                .font(.subheadline.weight(.semibold))
                .foregroundStyle(isRunning ? Theme.success : Theme.muted)
        }
    }
}
