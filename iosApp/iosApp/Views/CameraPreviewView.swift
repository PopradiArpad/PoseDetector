// Copyright 2025 Árpád Poprádi (arpad@popradiarpad.com)
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
import SwiftUI


// This View wrappes an UIView into the View world.
struct CameraPreviewView: UIViewRepresentable {

    let previewLayer: AVCaptureVideoPreviewLayer

    func makeUIView(context: Context) -> AutoFrameForAVCaptureVideoPreviewLayer {
        print("makeUIView")
        previewLayer.videoGravity = .resizeAspectFill

        let view = AutoFrameForAVCaptureVideoPreviewLayer()
        view.previewLayer = previewLayer
        view.backgroundColor = .black
        view.layer.addSublayer(previewLayer)

        return view
    }

    func updateUIView(_ uiView: AutoFrameForAVCaptureVideoPreviewLayer, context: Context) {
        // Not need: the wrapping UIView does it.
    }
}

// Dedicated UIView to manage the layer's frame:
// This UIView feel when its size get resized.
class AutoFrameForAVCaptureVideoPreviewLayer: UIView {
    var previewLayer: AVCaptureVideoPreviewLayer?

    override func layoutSubviews() {
        super.layoutSubviews()
        print("layoutSubviews bounds: \(bounds)")
        // Crucial: Update the frame every time the view's bounds change
        previewLayer?.frame = bounds
    }
}

