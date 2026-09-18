import UIKit
import SwiftUI
import Shared

/// The Compose screens, still here while the two UIs live side by side.
struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Self.Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Self.Context) {}
}

struct ContentView: View {
    var body: some View {
        TabView {
            MatchdayScreen()
                .tabItem { Label("Native", systemImage: "swift") }
            ComposeView()
                .ignoresSafeArea()
                .tabItem { Label("Compose", systemImage: "square.stack.3d.up") }
        }
    }
}
