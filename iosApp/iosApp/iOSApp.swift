import SwiftUI
import shared

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self)
    var appDelegate: AppDelegate

    // Integrating SwiftUI into Compose needs one step more
    // then the other way around because
    // shared doesn't see anything from iosApp,
    // but iosApp sees everything from shared.
    init() {
        LoggerKt.doInitLogger()
        LivePoseLandmarkerBackgroundFactory.shared.factory = {
            UIHostingController(rootView: LivePoseLandmarkerBackground())
                .view
        }
        InferenceTimeChartFactory.shared.factory = {
            UIHostingController(
                rootView:
                    InferenceTimeChart(
                        storage:
                            RealInferenceTimeStorage.shared
                    )
            ).view
        }
    }

    var body: some Scene {
        WindowGroup {
            ContentView(appDelegate.rootComponent)
        }
    }
}

class AppDelegate: NSObject, UIApplicationDelegate {
    private var stateKeeper = StateKeeperDispatcherKt.StateKeeperDispatcher(
        savedState: nil
    )

    lazy var rootComponent: RootComponent = RootComponent(
        // Create the root component context on the iOS side to let
        // the root context have all the bindings to the OS
        // which make it so powerful.
        componentContext: DefaultComponentContext(
            lifecycle: ApplicationLifecycle(),
            stateKeeper: stateKeeper,
            instanceKeeper: nil,
            backHandler: IOSBackHandlerProvider.shared.provide()
        )
    )

    func application(
        _ application: UIApplication,
        shouldSaveSecureApplicationState coder: NSCoder
    ) -> Bool {
        StateKeeperUtilsKt.save(coder: coder, state: stateKeeper.save())
        return true
    }

    func application(
        _ application: UIApplication,
        shouldRestoreSecureApplicationState coder: NSCoder
    ) -> Bool {
        stateKeeper = StateKeeperDispatcherKt.StateKeeperDispatcher(
            savedState: StateKeeperUtilsKt.restore(coder: coder)
        )
        return true
    }
}
