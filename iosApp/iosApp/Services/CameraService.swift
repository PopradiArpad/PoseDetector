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
import UIKit  // Needed for UIImage.Orientation

// MARK: CameraFeedServiceDelegate Declaration
protocol CameraServiceDelegate: AnyObject {

    /**
     This method delivers the pixel buffer of the current frame seen by the device's camera.
     ON WHICH THREAD IS IT DELIVERED?
     */
    func didOutput(
        sampleBuffer: CMSampleBuffer,
        orientation: UIImage.Orientation
    )

    /**
     This method initimates that a session runtime error occured.
     CAN COME ON ANY THREAD.
     */
    func didEncounterSessionRuntimeError()

    /**
     This method initimates that the session was interrupted.
     CAN COME ON ANY THREAD.
     */
    func sessionWasInterrupted(canResumeManually resumeManually: Bool)

    /**
     This method initimates that the session interruption has ended.
     CAN COME ON ANY THREAD.
     */
    func sessionInterruptionEnded()

}

// Based on CameraFeedService of PoseLandmarker
class CameraService: NSObject, ObservableObject,
    AVCaptureVideoDataOutputSampleBufferDelegate
{

    // MARK: the object which gets the video frames as data and events.

    weak var delegate: CameraServiceDelegate?

    // MARK: CameraStatus

    enum CameraStatus: String {
        case configured
        case configFailed
    }

    @Published private(set) var status: CameraStatus? = nil

    private func setStatus(_ newStatus: CameraStatus?) {
        DispatchQueue.main.async {
            self.status = newStatus
        }
    }

    // MARK: PreviewLayer (containing the video stream, must be put into a UIView)

    // A Layer to display the preview (passed to the UIViewRepresentable)
    lazy var liveVideoLayer = AVCaptureVideoPreviewLayer(session: session)
    let videoGravity = AVLayerVideoGravity.resizeAspectFill

    private let session = AVCaptureSession()
    private let sessionQueue = DispatchQueue(
        label: "com.popradiarpad.posedetectorio"
    )

    // MARK: Output video properties

    var videoResolution: CGSize {
        guard let size = imageBufferSize else {
            return CGSize.zero
        }
        let minDimension = min(size.width, size.height)
        let maxDimension = max(size.width, size.height)
        switch UIDevice.current.orientation {
        case .portrait:
            return CGSize(width: minDimension, height: maxDimension)
        case .landscapeLeft, .landscapeRight:
            return CGSize(width: maxDimension, height: minDimension)
        default:
            return CGSize(width: minDimension, height: maxDimension)
        }
    }

    private var imageBufferSize: CGSize?

    // MARK: Init/deinit

    override init() {
        super.init()
        setUpLiveVideoLayer()
        startConfigureSession()
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(orientationChanged),
            name: UIDevice.orientationDidChangeNotification,
            object: nil
        )
    }

    deinit {
        NotificationCenter.default.removeObserver(self)
    }

    private func setUpLiveVideoLayer() {
        liveVideoLayer.videoGravity = videoGravity
        setLiveVideoOrientation(.portrait)
    }

    // MARK: AVCapture session setup

    private func startConfigureSession() {
        sessionQueue.async {
            self.configureSession()
        }
    }

    private func configureSession() {
        session.beginConfiguration()

        // Tries to add an AVCaptureDeviceInput.
        guard addVideoDeviceInput() == true else {
            self.session.commitConfiguration()
            setStatus(.configFailed)
            return
        }

        // Tries to add an AVCaptureVideoDataOutput.
        guard addVideoDataOutput() else {
            self.session.commitConfiguration()
            setStatus(.configFailed)
            return
        }

        session.commitConfiguration()
        setStatus(.configured)
    }

    /**
     This method tries to an AVCaptureDeviceInput to the current AVCaptureSession.
     */
    private func addVideoDeviceInput() -> Bool {

        /**Tries to get the default back camera.
         */
        guard
            let camera = AVCaptureDevice.default(
                .builtInWideAngleCamera,
                for: .video,
                position: .back
            )
        else { return false }

        do {
            let videoDeviceInput = try AVCaptureDeviceInput(device: camera)
            if session.canAddInput(videoDeviceInput) {
                session.addInput(videoDeviceInput)
                return true
            } else {
                return false
            }
        } catch {
            fatalError("Cannot create video device input")
        }
    }

    /**
     This method tries to an AVCaptureVideoDataOutput to the current AVCaptureSession.
     */
    private func addVideoDataOutput() -> Bool {

        let videoDataOutput = AVCaptureVideoDataOutput()
        let sampleBufferQueue = DispatchQueue(label: "sampleBufferQueue")
        videoDataOutput.setSampleBufferDelegate(self, queue: sampleBufferQueue)
        videoDataOutput.alwaysDiscardsLateVideoFrames = true
        videoDataOutput.videoSettings = [
            String(kCVPixelBufferPixelFormatTypeKey): kCMPixelFormat_32BGRA
        ]

        if session.canAddOutput(videoDataOutput) {
            session.addOutput(videoDataOutput)
            videoDataOutput.connection(with: .video)?.videoRotationAngle = 90
            return true
        }
        return false
    }

    // MARK: Event detection

    @objc func orientationChanged(notification: Notification) {
        setLiveVideoOrientation(UIDevice.current.orientation)
    }

    private func setLiveVideoOrientation(
        _ deviceOrientation: UIDeviceOrientation
    ) {
        // Ensure the connection exists
        guard let connection = liveVideoLayer.connection else { return }

        let rotationAngle: CGFloat

        switch deviceOrientation {
        case .portrait:
            // Home button at the bottom
            rotationAngle = 90.0
        case .landscapeRight:
            // Home button to the right
            rotationAngle = 180.0
        case .landscapeLeft:
            // Home button to the left
            rotationAngle = 0.0
        case .portraitUpsideDown:
            // Home button at the top
            rotationAngle = 0.0
        default:
            // Ignore FaceUp, FaceDown, Unknown.
            return
        }

        if connection.isVideoRotationAngleSupported(rotationAngle) {
            connection.videoRotationAngle = rotationAngle
        }
    }

    // MARK: - AVCaptureVideoDataOutputSampleBufferDelegate

    func captureOutput(
        _ output: AVCaptureOutput,
        didOutput sampleBuffer: CMSampleBuffer,
        from connection: AVCaptureConnection
    ) {
        guard let imageBuffer = CMSampleBufferGetImageBuffer(sampleBuffer)
        else { return }
        
        // Store video frame image properties
        if imageBufferSize == nil {
            imageBufferSize = CGSize(
                width: CVPixelBufferGetHeight(imageBuffer),
                height: CVPixelBufferGetWidth(imageBuffer)
            )
        }

        // Forward image to the interested one.
        delegate?.didOutput(
            sampleBuffer: sampleBuffer,
            orientation: UIImage.Orientation.from(
                deviceOrientation: UIDevice.current.orientation
            )
        )
    }

    // MARK: Start/stop session

    func startSession() {
        sessionQueue.async {
            print("startSession self.isCameraSetup: \(self.status.asString)")
            guard self.status == .configured else { return }

            self.addObservers()
            self.session.startRunning()
        }
    }

    func stopSession() {
        self.removeObservers()

        sessionQueue.async {
            print("stopSession session.isRunning: \(self.session.isRunning)")

            if self.session.isRunning {
                self.session.stopRunning()
            }
        }
    }

    // MARK: Notification Observer Handling

    private func addObservers() {
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(
                CameraService.sessionWasInterrupted(notification:)
            ),
            name: AVCaptureSession.wasInterruptedNotification,
            object: session
        )
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(CameraService.sessionInterruptionEnded),
            name: AVCaptureSession.interruptionEndedNotification,
            object: session
        )
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(
                CameraService.sessionRuntimeErrorOccured(notification:)
            ),
            name: AVCaptureSession.runtimeErrorNotification,
            object: session
        )
    }

    private func removeObservers() {
        NotificationCenter.default.removeObserver(
            self,
            name: AVCaptureSession.wasInterruptedNotification,
            object: session
        )
        NotificationCenter.default.removeObserver(
            self,
            name: AVCaptureSession.interruptionEndedNotification,
            object: session
        )
        NotificationCenter.default.removeObserver(
            self,
            name: AVCaptureSession.runtimeErrorNotification,
            object: session
        )
    }

    // MARK: Notification Observers

    @objc func sessionWasInterrupted(notification: Notification) {
        printMethodNameAndThreadInfo()

        if let userInfoValue = notification.userInfo?[
            AVCaptureSessionInterruptionReasonKey
        ] as AnyObject?,
            let reasonIntegerValue = userInfoValue.integerValue,
            let reason = AVCaptureSession.InterruptionReason(
                rawValue: reasonIntegerValue
            )
        {
            print("Capture session was interrupted with reason \(reason)")

            var canResumeManually = false
            switch reason {
            case .videoDeviceInUseByAnotherClient:
                canResumeManually = true
            default:
                canResumeManually = false
            }

            self.delegate?.sessionWasInterrupted(
                canResumeManually: canResumeManually
            )
        }
    }

    @objc func sessionInterruptionEnded(notification: Notification) {
        printMethodNameAndThreadInfo()

        self.delegate?.sessionInterruptionEnded()
    }

    @objc func sessionRuntimeErrorOccured(notification: Notification) {
        printMethodNameAndThreadInfo()

        guard
            let error = notification.userInfo?[AVCaptureSessionErrorKey]
                as? AVError
        else {
            return
        }

        print("Capture session runtime error: \(error)")

        guard error.code == .mediaServicesWereReset else {
            self.delegate?.didEncounterSessionRuntimeError()
            return
        }
    }
}

// MARK: UIImage.Orientation Extension
extension UIImage.Orientation {
    static func from(deviceOrientation: UIDeviceOrientation)
        -> UIImage.Orientation
    {
        switch deviceOrientation {
        case .portrait:
            return .up
        case .landscapeLeft:
            return .left
        case .landscapeRight:
            return .right
        default:
            return .up
        }
    }
}
