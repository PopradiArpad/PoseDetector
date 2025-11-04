// Copyright 2025 Modified by Árpád Poprádi (arpad@popradiarpad.com) from an original work by 2023 The MediaPipe Authors.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import AVFoundation
import Combine
import MediaPipeTasksVision
import UIKit

/**
 It deliveres the camera live stream and the pose landmarker views as UIView like objects.
  It orchestrate the camera and pose landmarker services which create that views.
*/
class CameraViewModel: NSObject, ObservableObject {

    // MARK: CameraError

    enum CameraError: String {
        case configFailed
        case permissionDenied
    }

    @Published private(set) var cameraError: CameraError? = nil

    // MARK: Views to show

    private(set) lazy var liveVideoLayer = cameraService.liveVideoLayer
    let overlayView = OverlayView()

    // MARK: Camera service handling

    private let cameraService = CameraService()
    // -> cameraOffloadQueue: to off load the camera thread 30-60 times per second
    // The work is small but very frequent, that's why it's needed.
    private let backgroundQueue = DispatchQueue(
        label: "com.popradiarpad.cameraViewController.backgroundQueue"
    )

    // MARK: Pose Landmarker service handling

    // The poselandmarker get recreated when new config is there
    // and the assignement is not atomic.
    // So, let's protect the poselandmarker assignement.
    // The method call usage is protected by Swift:
    // used object can not be destroyed (done by temporary hard reference).
    private var _poseLandmarkerService: PoseLandmarkerService?
    private var poseLandmarkerService: PoseLandmarkerService? {
        get {
            poseLandmarkerServiceQueue.sync {
                return self._poseLandmarkerService
            }
        }
        set {
            poseLandmarkerServiceQueue.async(flags: .barrier) {
                self._poseLandmarkerService = newValue
            }
        }
    }
    private let poseLandmarkerServiceQueue = DispatchQueue(
        label:
            "com.popradiarpad.cameraViewController.poseLandmarkerServiceQueue",
        attributes: .concurrent
    )

    // Crucial for managing the subscription's lifecycle (like a Disposable in Rx/Flow)
    private var cancellables = Set<AnyCancellable>()

    // MARK: Init/deinit

    override init() {
        super.init()
        bindCameraServiceStatusChange()
    }

    private func bindCameraServiceStatusChange() {
        cameraService.$status
            .receive(on: DispatchQueue.main)  // Explicit main-thread (optional, @Published handles it)
            .map { [weak self] status -> CameraError? in
                switch status {
                case .configured:
                    self?.cameraService.startSession()
                    return nil  // Clear error on success
                case .configFailed:
                    return .configFailed
                case .permissionDenied:
                    return .permissionDenied
                case .none:
                    return nil  // Initial state
                }
            }
            .assign(to: \.cameraError, on: self)
            .store(in: &cancellables)
    }


    // MARK: Orchestrate appear/disapper on the screen.

    #if !targetEnvironment(simulator)
        func onAppear() {
            cameraService.delegate = self
            initializePoseLandmarkerServiceOnSessionResumption()
        }

        func onDisappear() {
            cameraService.stopSession()
            clearPoseLandmarkerServiceOnSessionInterruption()
        }
    #endif

    private func initializePoseLandmarkerServiceOnSessionResumption() {
        clearAndInitializePoseLandmarkerService()
        startObserveConfigChanges()
    }

    @objc private func clearAndInitializePoseLandmarkerService() {
        poseLandmarkerService = nil
        poseLandmarkerService =
            PoseLandmarkerService
            .liveStreamPoseLandmarkerService(
                modelPath: InferenceConfigurationManager.sharedInstance.model
                    .modelPath,
                numPoses: InferenceConfigurationManager.sharedInstance.numPoses,
                minPoseDetectionConfidence: InferenceConfigurationManager
                    .sharedInstance.minPoseDetectionConfidence,
                minPosePresenceConfidence: InferenceConfigurationManager
                    .sharedInstance.minPosePresenceConfidence,
                minTrackingConfidence: InferenceConfigurationManager
                    .sharedInstance.minTrackingConfidence,
                liveStreamDelegate: self,
                delegate: InferenceConfigurationManager.sharedInstance.delegate
            )
    }

    private func clearPoseLandmarkerServiceOnSessionInterruption() {
        stopObserveConfigChanges()
        poseLandmarkerService = nil
    }

    // MARK: Adapt to inference configuration change

    private var isObserving = false

    private func startObserveConfigChanges() {
        NotificationCenter.default
            .addObserver(
                self,
                selector: #selector(clearAndInitializePoseLandmarkerService),
                name: InferenceConfigurationManager.notificationName,
                object: nil
            )
        isObserving = true
    }

    private func stopObserveConfigChanges() {
        if isObserving {
            NotificationCenter.default
                .removeObserver(
                    self,
                    name: InferenceConfigurationManager.notificationName,
                    object: nil
                )
        }
        isObserving = false
    }
}

extension CameraViewModel: CameraServiceDelegate {

    func didOutput(
        sampleBuffer: CMSampleBuffer,
        orientation: UIImage.Orientation
    ) {
        let currentTimeMs = Date().timeIntervalSince1970 * 1000
        // Pass the pixel buffer to mediapipe
        backgroundQueue.async { [weak self] in
            self?.poseLandmarkerService?.detectAsync(
                sampleBuffer: sampleBuffer,
                orientation: orientation,
                timeStamps: Int(currentTimeMs)
            )
        }
    }

    // MARK: Session Handling Alerts
    func sessionWasInterrupted(canResumeManually resumeManually: Bool) {
        clearPoseLandmarkerServiceOnSessionInterruption()
    }

    func sessionInterruptionEnded() {
        initializePoseLandmarkerServiceOnSessionResumption()
    }

    func didEncounterSessionRuntimeError() {
        clearPoseLandmarkerServiceOnSessionInterruption()
    }
}

// MARK: PoseLandmarkerServiceLiveStreamDelegate
extension CameraViewModel: PoseLandmarkerServiceLiveStreamDelegate {

    func poseLandmarkerService(
        _ poseLandmarkerService: PoseLandmarkerService,
        didFinishDetection result: ResultBundle?,
        error: Error?
    ) {
        DispatchQueue.main.async { [weak self] in
            guard let weakSelf = self else { return }
            guard
                let poseLandmarkerResult = result?.poseLandmarkerResults.first
                    as? PoseLandmarkerResult
            else { return }
            let imageSize = weakSelf.cameraService.videoResolution
            let poseOverlays = OverlayView.poseOverlays(
                fromMultiplePoseLandmarks: poseLandmarkerResult.landmarks,
                inferredOnImageOfSize: imageSize,
                ovelayViewSize: weakSelf.overlayView.bounds.size,
                imageContentMode: weakSelf.overlayView.imageContentMode,
                andOrientation: UIImage.Orientation.from(
                    deviceOrientation: UIDevice.current.orientation
                )
            )
            weakSelf.overlayView.draw(
                poseOverlays: poseOverlays,
                inBoundsOfContentImageOfSize: imageSize,
                imageContentMode: weakSelf.cameraService.videoGravity
                    .contentMode
            )
        }
    }
}

// MARK: - AVLayerVideoGravity Extension
extension AVLayerVideoGravity {
    var contentMode: UIView.ContentMode {
        switch self {
        case .resizeAspectFill:
            return .scaleAspectFill
        case .resizeAspect:
            return .scaleAspectFit
        case .resize:
            return .scaleToFill
        default:
            return .scaleAspectFill
        }
    }
}
