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

import SwiftUI

/**
  To wrap a OverlayView into a View
 */
struct PoseOverlayView: UIViewRepresentable {
    let poseLayout: OverlayView

    func makeUIView(context: Context) -> UIView {
        poseLayout.backgroundColor = .clear
        poseLayout.isOpaque = false // Explicitly allow transparency
        return poseLayout
    }

    func updateUIView(_ uiView: UIView, context: Context) {
        uiView.backgroundColor = .clear
        uiView.isOpaque = false
    }
}
