import SwiftUI
import shared

@main
struct iOSApp: App {
    init() {
        LivePoseLandmarkerScreenFactoryProvider.shared.factory = LivePoseLandmarkerScreenFactoryImpl()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
