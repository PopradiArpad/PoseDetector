//
//  InferenceTimeChart.swift
//  iosApp
//
//  Created by Árpád Poprádi on 05.12.25.
//

import SwiftUI
import shared

struct InferenceTimeChart: View {
    let storage: InferenceTimeStorage

    // The latest window of data points for the chart
    @State private var points: [InferenceDataPoint] = []

    var body: some View {
        VStack(spacing: 0 /*take the full height for content*/) {
            Text("Inference Time (last 10s)")
                .font(.headline)
                .frame(maxWidth: .infinity, alignment: .center)
                .padding(.vertical, 12)
                .background(.ultraThinMaterial)  // subtle background on light/dark

            GeometryReader { geo in
                ZStack {
                    // Background grid
                    gridLines(width: geo.size.width, height: geo.size.height)

                    // The actual line chart
                    if !points.isEmpty {
                        chartLine(
                            width: geo.size.width,
                            height: geo.size.height
                        )
                        .stroke(Color.blue, lineWidth: 2.5)
                    }
                }
            }
            // Take the full rest height.
            .frame(maxHeight: .infinity)
        }
        // Take the full height given to this View.
        .frame(maxHeight: .infinity)
        .task {
            for await newPoints in storage.dataPoints {
                withAnimation(.linear(duration: 0.15)) {
                    points = newPoints
                }
            }
        }
    }

    // Simple horizontal + vertical grid
    private func gridLines(width: CGFloat, height: CGFloat) -> some View {
        Path { path in
            // Horizontal lines
            for i in 0...4 {
                let y = height * CGFloat(i) / 4
                path.move(to: CGPoint(x: 0, y: y))
                path.addLine(to: CGPoint(x: width, y: y))
            }
            // Vertical lines (every 2 seconds)
            for i in 0...5 {
                let x = width * CGFloat(i) / 5
                path.move(to: CGPoint(x: x, y: 0))
                path.addLine(to: CGPoint(x: x, y: height))
            }
        }
        .stroke(Color.gray.opacity(0.3), lineWidth: 1)
    }

    // The real chart line
    private func chartLine(width: CGFloat, height: CGFloat) -> Path {
        Path { path in
            guard !points.isEmpty else { return }

            // Find min/max for scaling (only from current window)
            let times = points.map { $0.inferenceTimeMs }
            let minTime = times.min() ?? 0
            let maxTime = max(times.max() ?? 100, minTime + 1)  // avoid divide by zero

            for (index, point) in points.enumerated() {
                let x = width * CGFloat(index) / CGFloat(points.count - 1)
                let normalizedTime =
                    (point.inferenceTimeMs - minTime) / (maxTime - minTime)
                let y = height * (1 - CGFloat(normalizedTime))  // invert Y (0ms at bottom)

                if index == 0 {
                    path.move(to: CGPoint(x: x, y: y))
                } else {
                    path.addLine(to: CGPoint(x: x, y: y))
                }
            }
        }
    }
}

#Preview("Realistic fluctuating data") {
    InferenceTimeChart(
        storage:
            PreviewInferenceTimeStorage(
                dataPoints: (0..<30).map { i in
                    InferenceDataPoint(
                        inferenceTimeMs: 22 + Double.random(in: -15...30),
                        timestampEpochMs: 1_700_000_000_000 + i * 333
                    )
                }
            )
    )
    .padding()
    .frame(height: 240)
}

#Preview("Empty state") {
    InferenceTimeChart(storage: PreviewInferenceTimeStorage(dataPoints: []))
        .padding()
        .frame(height: 240)
}
