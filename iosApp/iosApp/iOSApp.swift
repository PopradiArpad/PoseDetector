import SwiftUI
import shared

@main
struct iOSApp: App {
    // Integrating SwiftUI into Compose needs one step more
    // then the other way around because
    // shared doesn't see anything from iosApp,
    // but iosApp sees everything from shared.
    init() {
        LivePoseLandmarkerBackgroundFactory.shared.factory = {
            return UIHostingController(rootView: LivePoseLandmarkerBackground())
        }
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
