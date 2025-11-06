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

struct LivePoseLandmarkerBackground: View {
    @StateObject private var viewModel = CameraViewModel()
    @State private var showingSettingsAlert = false

    var body: some View {
        ZStack {
            CameraPreviewView(previewLayer: viewModel.liveVideoLayer)
                // fill the entire screen, including safe areas.
                .ignoresSafeArea(.all, edges: .all)

            PoseOverlayView(poseLayout: viewModel.overlayView)
                // fill the entire screen, including safe areas.
                .ignoresSafeArea(.all, edges: .all)

            // SwiftUI Overlay for UI
            VStack() {
                Spacer()
                CameraErrorView(cameraError: viewModel.cameraError)
                Spacer()
                Spacer()
            }
        }
        .onChange(of: viewModel.cameraError) {
            print("onChange status: \(viewModel.cameraError.asString)")
            guard let error = viewModel.cameraError else { return }

            switch error {
            case .permissionDenied:
                showingSettingsAlert = true
            default:
                break
            }
        }
        .onAppear {
            viewModel.onAppear()
        }
        .onDisappear {
            viewModel.onDisappear()
        }
        .alert("Camera Permission Required", isPresented: $showingSettingsAlert)
        {
            Button("Settings") {
                openAppSettings()
            }
            Button("Cancel", role: .cancel) {
                // Do nothing, just dismiss the alert
            }
        } message: {
            Text(
                "To use the pose detection feature, please grant camera access in your device settings."
            )
        }
    }

    // Helper function to open the app's settings
    private func openAppSettings() {
        if let settingsURL = URL(string: UIApplication.openSettingsURLString) {
            UIApplication.shared.open(settingsURL)
        }
    }
}

struct CameraErrorView: View {
    let cameraError: CameraViewModel.CameraError?

    var body: some View {
        if let error = cameraError {
            Text("🚨 Camera Error: \(error)")
                .font(.headline)
                .foregroundColor(.red)
        } else {
            EmptyView()
        }
    }
}

#Preview {
    ContentView()
}
