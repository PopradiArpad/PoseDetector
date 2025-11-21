import UIKit
import SwiftUI
import shared

struct ComposeView: UIViewControllerRepresentable {
    private let rootComponent: RootComponent
    
    init(_ rootComponent: RootComponent) {
        self.rootComponent = rootComponent
    }

    func makeUIViewController(context: Context) -> UIViewController {
        return MainViewControllerKt.MainViewController(rootComponent : rootComponent)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    private let rootComponent: RootComponent
    
    init(_ rootComponent: RootComponent) {
        self.rootComponent = rootComponent
    }

    var body: some View {
        ComposeView(rootComponent)
            .ignoresSafeArea(.all) // Extend to the edges
    }
}
