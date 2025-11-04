
import Foundation
import shared
import SwiftUI
import UIKit

class LivePoseLandmarkerScreenFactoryImpl: LivePoseLandmarkerScreenFactory {
    func create() -> UIViewController {
        let swiftUIView = LivePoseLandmarkerScreenIos()
        let hostingController = UIHostingController(rootView: swiftUIView)
        return hostingController
    }
}
