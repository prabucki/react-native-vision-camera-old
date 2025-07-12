//
//  FrameProcessorPerformanceDataCollector.swift
//  VisionCameraOld
//
//  Created by Marc Rousavy on 30.08.21.
//  Copyright © 2021 mrousavy. All rights reserved.
//

import Foundation
import os.log

// keep a maximum of `maxSampleSize` historical performance data samples cached.
private let maxSampleSize = 20
private let warmupSamples = 5

// MARK: - PerformanceSampleCollection

struct PerformanceSampleCollection {
  var endPerformanceSampleCollection: () -> Void

  init(end: @escaping () -> Void) {
    endPerformanceSampleCollection = end
  }
}

// MARK: - FrameProcessorPerformanceDataCollector

class FrameProcessorPerformanceDataCollector {
  private var performanceSamples: [Double] = []
  private let performanceSamplesQueue = DispatchQueue(label: "performance-samples", qos: .utility)
  private var counter = 0
  private var lastEvaluation = -1
  private var totalFramesProcessed: Int64 = 0
  private var totalProcessingTime: UInt64 = 0
  private var minExecutionTime: Double = Double.greatestFiniteMagnitude
  private var maxExecutionTime: Double = 0.0

  var hasEnoughData: Bool {
    return performanceSamplesQueue.sync {
      return performanceSamples.count >= warmupSamples
    }
  }

  var averageExecutionTimeSeconds: Double {
    return performanceSamplesQueue.sync {
      guard !performanceSamples.isEmpty else { return 0.0 }

      // Skip warmup samples for more accurate measurement
      let validSamples = performanceSamples.count > warmupSamples
        ? Array(performanceSamples.dropFirst(warmupSamples))
        : performanceSamples

      guard !validSamples.isEmpty else { return 0.0 }

      // Use trimmed mean to reduce impact of outliers
      let sorted = validSamples.sorted()
      let trimPercent = 0.1 // Remove top and bottom 10%
      let trimCount = Int(Double(sorted.count) * trimPercent)

      let trimmed = (trimCount > 0 && sorted.count > trimCount * 2)
        ? Array(sorted.dropFirst(trimCount).dropLast(trimCount))
        : sorted

      let sum = trimmed.reduce(0, +)
      let average = sum / Double(trimmed.count)

      lastEvaluation = counter
      return average
    }
  }

  var processingStats: [String: Any] {
    return performanceSamplesQueue.sync {
      return [
        "totalFrames": totalFramesProcessed,
        "totalTime": Double(totalProcessingTime) / 1_000_000_000.0, // Convert to seconds
        "averageTime": averageExecutionTimeSeconds,
        "minTime": minExecutionTime,
        "maxTime": maxExecutionTime,
        "sampleCount": performanceSamples.count
      ]
    }
  }

  func beginPerformanceSampleCollection() -> PerformanceSampleCollection {
    let begin = DispatchTime.now()

    return PerformanceSampleCollection {
      let end = DispatchTime.now()
      let nanoseconds = end.uptimeNanoseconds - begin.uptimeNanoseconds
      let seconds = Double(nanoseconds) / 1_000_000_000.0

      self.performanceSamplesQueue.async {
        let index = self.counter % maxSampleSize

        if self.performanceSamples.count > index {
          self.performanceSamples[index] = seconds
        } else {
          self.performanceSamples.append(seconds)
        }

        // Update statistics
        self.totalFramesProcessed += 1
        self.totalProcessingTime += nanoseconds
        self.minExecutionTime = min(self.minExecutionTime, seconds)
        self.maxExecutionTime = max(self.maxExecutionTime, seconds)

        self.counter += 1
      }
    }
  }

  func clear() {
    performanceSamplesQueue.sync {
      counter = 0
      performanceSamples.removeAll()
      totalFramesProcessed = 0
      totalProcessingTime = 0
      minExecutionTime = Double.greatestFiniteMagnitude
      maxExecutionTime = 0.0
    }
  }

  func getSuggestedFrameRate(maxFrameRate: Double = 30.0) -> Double {
    guard hasEnoughData else { return maxFrameRate }

    let avgTime = averageExecutionTimeSeconds
    guard avgTime > 0 else { return maxFrameRate }

    // Calculate theoretical max FPS based on processing time
    let theoreticalMaxFps = 1.0 / avgTime

    // Add safety margin (20%) to prevent frame drops
    let safeMaxFps = theoreticalMaxFps * 0.8

    // Clamp to reasonable bounds
    return min(safeMaxFps, maxFrameRate).clamped(to: 1.0...maxFrameRate)
  }
}

// MARK: - Double Extension

private extension Double {
  func clamped(to range: ClosedRange<Double>) -> Double {
    return Swift.min(Swift.max(self, range.lowerBound), range.upperBound)
  }
}
