
import Foundation
import shared
import SwiftUI
import UIKit

class LivePoseLandmarkerScreenFactoryImpl: LivePoseLandmarkerScreenFactory {
    func create(onFinish: @escaping () -> Void) -> UIViewController {
        let swiftUIView = LivePoseLandmarkerScreenIos(onFinish: onFinish)
        let hostingController = UIHostingController(rootView: swiftUIView)
        return hostingController
    }
}
