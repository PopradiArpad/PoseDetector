import SwiftUI
import shared

@main
struct iOSApp: App {
    // @UIApplicationDelegateAdaptor(AppDelegate.self)
    // var appDelegate: AppDelegate

    // Integrating SwiftUI into Compose needs one step more
    // then the other way around because
    // shared doesn't see anything from iosApp,
    // but iosApp sees everything from shared.
    init() {
        LoggerKt.doInitLogger()
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

// class AppDelegate: NSObject, UIApplicationDelegate {
//     private var stateKeeper = StateKeeperDispatcherKt.StateKeeperDispatcher(savedState: nil)
//
//     lazy var root: RootComponent = DefaultRootComponent(
//         componentContext: DefaultComponentContext(
//             lifecycle: ApplicationLifecycle(),
//             stateKeeper: stateKeeper,
//             instanceKeeper: nil,
//             backHandler: nil
//         ),
//         featureInstaller: DefaultFeatureInstaller.shared,
//         deepLinkUrl: nil
//     )
//
//     func application(_ application: UIApplication, shouldSaveSecureApplicationState coder: NSCoder) -> Bool {
//         StateKeeperUtilsKt.save(coder: coder, state: stateKeeper.save())
//         return true
//     }
//
//     func application(_ application: UIApplication, shouldRestoreSecureApplicationState coder: NSCoder) -> Bool {
//         stateKeeper = StateKeeperDispatcherKt.StateKeeperDispatcher(savedState: StateKeeperUtilsKt.restore(coder: coder))
//         return true
//     }
// }
