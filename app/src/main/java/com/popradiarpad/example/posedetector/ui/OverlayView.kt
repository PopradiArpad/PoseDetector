/*
 * Copyright 2023 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.popradiarpad.example.posedetector.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import kotlin.math.max
import kotlin.math.min

class OverlayView(context: Context?) :
    View(context) {

    private var results: PoseLandmarkerResult? = null
    private var pointPaint = Paint()
    private var linePaint = Paint()

    private var scaleFactor: Float = 1f
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1

    init {
        initPaints()
    }

    fun clear() {
        results = null
        pointPaint.reset()
        linePaint.reset()
        invalidate()
        initPaints()
    }

    private fun initPaints() {
        linePaint.color = 0x007F8B
        linePaint.strokeWidth = LANDMARK_STROKE_WIDTH
        linePaint.style = Paint.Style.STROKE

        pointPaint.color = Color.YELLOW
        pointPaint.strokeWidth = LANDMARK_STROKE_WIDTH
        pointPaint.style = Paint.Style.FILL
    }

    fun setResults(
            poseLandmarkerResults: PoseLandmarkerResult,
            imageHeight: Int,
            imageWidth: Int,
            runningMode: RunningMode = RunningMode.IMAGE
    ) {
        results = poseLandmarkerResults

        this.imageHeight = imageHeight
        this.imageWidth = imageWidth

        scaleFactor = when (runningMode) {
            RunningMode.IMAGE,
            RunningMode.VIDEO -> {
                min(width * 1f / imageWidth, height * 1f / imageHeight)
            }

            RunningMode.LIVE_STREAM -> {
                // PreviewView is in FILL_START mode. So we need to scale up the
                // landmarks to match with the size that the captured images will be
                // displayed.
                max(width * 1f / imageWidth, height * 1f / imageHeight)
            }
        }
        invalidate()
    }

    // To understand the why and how of the coordinate transformations see the explanation at
    // the bottom of this file.
    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        val poseLandmarkerResult = results ?: return
        // Check if there are any landmarks to draw
        if (poseLandmarkerResult.landmarks().isEmpty()) return


        // Calculate the scaled image dimensions
        // These are the dimensions of the full image if it were scaled by scaleFactor
        val scaledImageWidth = imageWidth * scaleFactor
        val scaledImageHeight = imageHeight * scaleFactor

        // Calculate the offset of the scaled image within the view
        // This centers the scaled image within the OverlayView bounds
        val offsetX = (width - scaledImageWidth) / 2f
        val offsetY = (height - scaledImageHeight) / 2f

        // The result bundle provides a list of PoseLandmarkerResult, but for live stream, we usually expect one.
        // And PoseLandmarkerResult contains a list of landmark lists (one list per detected pose).
        // Typically, for single pose detection, landmarks().get(0) is used.
        for (landmarkList in poseLandmarkerResult.landmarks()) { // Iterate over each detected pose
            // Draw landmarks
            for (normalizedLandmark in landmarkList) {
                canvas.drawPoint(
                        offsetX + normalizedLandmark.x() * scaledImageWidth, // Apply offsetX
                        offsetY + normalizedLandmark.y() * scaledImageHeight, // Apply offsetY
                        pointPaint
                )
            }

            // Draw lines
            PoseLandmarker.POSE_LANDMARKS.forEach { connection ->
                // Ensure landmarkList is not empty and indices are valid
                if (landmarkList.size > connection.start() && landmarkList.size > connection.end()) {
                    val startLm = landmarkList[connection.start()]
                    val endLm = landmarkList[connection.end()]
                    canvas.drawLine(
                            offsetX + startLm.x() * scaledImageWidth, // Apply offsetX
                            offsetY + startLm.y() * scaledImageHeight, // Apply offsetY
                            offsetX + endLm.x() * scaledImageWidth,   // Apply offsetX
                            offsetY + endLm.y() * scaledImageHeight,  // Apply offsetY
                            linePaint
                    )
                }
            }
        }
    }

    companion object {
        private const val LANDMARK_STROKE_WIDTH = 12F
    }
}

/*
asciidoc
= Understanding Pose Landmark Transformation for Overlay Views in Kotlin with Jetpack Compose

== The Goal

The primary objective is to accurately render pose landmarks, provided by a system like MediaPipe,
onto an `OverlayView` (often a custom Android `View` integrated into Compose via `AndroidView`).
This overlay sits on top of a live camera preview, typically managed by CameraX and displayed using
a `PreviewView` within the Compose UI. MediaPipe gives landmark coordinates relative to the image
it processed. These coordinates must be transformed to match the screen's coordinate system and
how the `PreviewView` displays the camera feed.

== Why is This Transformation Needed?

Several factors necessitate this transformation:

*   **Different Coordinate Systems:**
** MediaPipe provides *normalized* coordinates (e.g., `x`, `y` values between 0.0 and 1.0)
relative to the dimensions of the specific image it analyzed. `(0.0, 0.0)` is the top-left,
and `(1.0, 1.0)` is the bottom-right of that processed image.
** The Android `OverlayView` (or a Composable drawing on a `Canvas`) draws using *pixel
coordinates* relative to its own size and position on the device screen.

*   **Different Aspect Ratios & Scaling:**
** The camera image (e.g., 4:3 aspect ratio) often has a different aspect ratio than the
device screen or the `PreviewView` displaying the camera feed (e.g., 19:9).
** The `PreviewView` scales the camera image to fit its bounds. Common scaling types, like
`FILL_CENTER` (the default for CameraX `PreviewView`), scale the image to fill the view
while maintaining aspect ratio. This can lead to:
*** Cropping: Parts of the image might be cut off if the scaled image is larger than the
view in one dimension.
*** Letterboxing/Pillarboxing: If a `FIT_CENTER`-like mode were used, there would be
empty bars around the image.
** The overlay must perfectly align with how the `PreviewView` scales and potentially crops
the image.

*   **Rotation & Mirroring (Often Handled Pre-MediaPipe):**
** The camera sensor's natural orientation might differ from the display orientation.
The image MediaPipe processes is usually already rotated to be upright (often handled by
`ImageAnalysis` configuration or within the `PoseLandmarkerHelper`).
** For front cameras, the image might be mirrored so the user sees a "mirror image."
This transformation is also typically handled before MediaPipe receives the image, or it's
accounted for in the rendering logic (e.g., by flipping the canvas in `OverlayView`).

== Step-by-Step Transformation: Landmark to Overlay Coordinates

Let `inputImageWidth` and `inputImageHeight` be the dimensions of the (already correctly
oriented) image that MediaPipe's `PoseLandmarker` processed. These are the dimensions passed to
the `OverlayView`'s `setResults()` method (or an equivalent data structure if using a pure
Composable overlay).

=== Step 0: Normalized Landmark Coordinates (from MediaPipe)

MediaPipe outputs landmarks where:
*   `landmark.x()` is a value between 0.0 and 1.0 (fraction of `inputImageWidth`).
*   `landmark.y()` is a value between 0.0 and 1.0 (fraction of `inputImageHeight`).

=== Step 1: Denormalization - Convert to Pixel Coordinates within the Processed Image

To get pixel coordinates relative to the *image MediaPipe saw*:
*   `val pixelXInImage = landmark.x() * inputImageWidth`
*   `val pixelYInImage = landmark.y() * inputImageHeight`

_Example: If `inputImageWidth` is 640 and `landmark.x()` is 0.5, then `pixelXInImage` is 320._

=== Step 2: Determine How the Processed Image is Scaled to Fit the OverlayView

The `OverlayView` (or the Composable `Canvas` area, which typically matches the `PreviewView`
dimensions) has its own `width` and `height` on the screen.
In `OverlayView.setResults()`, a `scaleFactor` is calculated. For `LIVE_STREAM` mode with
`PreviewView.ScaleType.FILL_CENTER` (which scales to fill, potentially cropping):

*   `val scaleFactor = max(overlayView.width / inputImageWidth, overlayView.height / inputImageHeight)`

This `scaleFactor` indicates how the processed image is scaled to ensure it fills the
`OverlayView` in at least one dimension while maintaining its aspect ratio.

The dimensions of this *scaled image* are:
*   `val scaledImageWidth = inputImageWidth * scaleFactor`
*   `val scaledImageHeight = inputImageHeight * scaleFactor`

One of these dimensions (`scaledImageWidth` or `scaledImageHeight`) will match the corresponding
`OverlayView` dimension, while the other will be equal to or larger than the `OverlayView`'s
other dimension (representing the portion that gets visually cropped).

=== Step 3: Calculate the Offset of the Scaled (and Potentially Cropped) Image

This is a critical step for alignment, handled within the `OverlayView`'s drawing logic.
Because `PreviewView` (with `FILL_CENTER`) centers the scaled camera image, the top-left
corner `(0,0)` of the *visible portion* of the camera image might not align with the `(0,0)` of
the `OverlayView`.

The offsets are calculated as:
*   `val offsetX = (overlayView.width - scaledImageWidth) / 2f`
*   `val offsetY = (overlayView.height - scaledImageHeight) / 2f`

_Example: If `overlayView.width` is 1080px, but `scaledImageWidth` becomes 1200px (because the
image was scaled up to match the view's height and its aspect ratio made it wider), then
`offsetX` would be `(1080 - 1200) / 2 = -60px`. This means the visible part of the camera image
effectively starts 60 pixels to the right of where the full scaled image's left edge would be
if it were drawn from `(0,0)`. The overlay drawing must account for this shift._

=== Step 4: Transform Landmark to Final Screen Coordinates for Drawing

The final drawing coordinates on the canvas are calculated by:

1.  Scaling the normalized landmark by the *full scaled image dimensions*:
*   `val pointXOnScaledImage = landmark.x() * scaledImageWidth`
*   `val pointYOnScaledImage = landmark.y() * scaledImageHeight`
This gives the landmark's position as if the entire scaled image were drawn starting at
`(0,0)` of the view.

2.  Applying the calculated `offsetX` and `offsetY` to align with the centered and potentially
cropped display:
*   `val finalXOnCanvas = offsetX + pointXOnScaledImage`
*   `val finalYOnCanvas = offsetY + pointYOnScaledImage`

These `finalXOnCanvas` and `finalYOnCanvas` values are then used with `canvas.drawPoint()`,
`canvas.drawLine()`, etc., within the `OverlayView`'s `onDraw` method (or a Compose `Canvas`
`drawScope`).

== Summary of the `OverlayView.draw()` Method's Logic (Kotlin-centric)

The `draw()` method in the `OverlayView` effectively performs these steps:

1.  Receives normalized landmarks `(lx, ly)` from the `results` (which were set by a method
like `setResults()`).
2.  Uses `inputImageWidth` and `inputImageHeight` (from `setResults()`).
3.  Accesses its own current `width` and `height` (the dimensions of the `OverlayView` on screen).
4.  Uses the pre-calculated `scaleFactor` (from `setResults()`).
5.  Calculates `scaledImageWidth = inputImageWidth * scaleFactor`.
6.  Calculates `scaledImageHeight = inputImageHeight * scaleFactor`.
7.  Calculates `offsetX = (this.width - scaledImageWidth) / 2f`.
8.  Calculates `offsetY = (this.height - scaledImageHeight) / 2f`.
9.  For each landmark `(lx, ly)`, it computes the final drawing coordinates:
*   `val drawX = offsetX + lx * scaledImageWidth`
*   `val drawY = offsetY + ly * scaledImageHeight`
10. Draws the point or line at `(drawX, drawY)` on the `Canvas`.

This sequence ensures that the landmarks, originally defined relative to the coordinate system of
the image processed by MediaPipe, are accurately scaled and positioned over the corresponding
features in the camera preview as displayed on the screen using Jetpack Compose and CameraX.
*/