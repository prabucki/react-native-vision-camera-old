package com.mrousavy.old.camera.frameprocessor

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.max
import kotlin.math.min

data class PerformanceSampleCollection(val endPerformanceSampleCollection: () -> Unit)

// keep a maximum of `maxSampleSize` historical performance data samples cached.
private const val maxSampleSize = 20
private const val warmupSamples = 5

class FrameProcessorPerformanceDataCollector {
  private var counter = AtomicInteger(0)
  private var performanceSamples: ArrayList<Double> = ArrayList(maxSampleSize)
  private val totalFramesProcessed = AtomicLong(0)
  private val totalProcessingTime = AtomicLong(0)
  private var minExecutionTime = Double.MAX_VALUE
  private var maxExecutionTime = Double.MIN_VALUE

  val averageExecutionTimeSeconds: Double
    get() = synchronized(performanceSamples) {
      if (performanceSamples.isEmpty()) return 0.0

      // Skip warmup samples for more accurate measurement
      val validSamples = if (performanceSamples.size > warmupSamples) {
        performanceSamples.drop(warmupSamples)
      } else {
        performanceSamples
      }

      if (validSamples.isEmpty()) return 0.0

      // Use trimmed mean to reduce impact of outliers
      val sorted = validSamples.sorted()
      val trimPercent = 0.1 // Remove top and bottom 10%
      val trimCount = (sorted.size * trimPercent).toInt()

      val trimmed = if (trimCount > 0 && sorted.size > trimCount * 2) {
        sorted.drop(trimCount).dropLast(trimCount)
      } else {
        sorted
      }

      return trimmed.average()
    }

  val hasEnoughData: Boolean
    get() = performanceSamples.size >= warmupSamples

  val processingStats: Map<String, Any>
    get() = mapOf(
      "totalFrames" to totalFramesProcessed.get(),
      "totalTime" to totalProcessingTime.get() / 1_000_000.0, // Convert to seconds
      "averageTime" to averageExecutionTimeSeconds,
      "minTime" to minExecutionTime,
      "maxTime" to maxExecutionTime,
      "sampleCount" to performanceSamples.size
    )

  fun beginPerformanceSampleCollection(): PerformanceSampleCollection {
    val begin = System.nanoTime()

    return PerformanceSampleCollection {
      val end = System.nanoTime()
      val nanoseconds = end - begin
      val seconds = nanoseconds / 1_000_000_000.0

      synchronized(performanceSamples) {
        val index = counter.getAndIncrement() % maxSampleSize

        if (performanceSamples.size > index) {
          performanceSamples[index] = seconds
        } else {
          performanceSamples.add(seconds)
        }

        // Update statistics
        totalFramesProcessed.incrementAndGet()
        totalProcessingTime.addAndGet(nanoseconds)
        minExecutionTime = min(minExecutionTime, seconds)
        maxExecutionTime = max(maxExecutionTime, seconds)
      }
    }
  }

  fun clear() {
    synchronized(performanceSamples) {
      counter.set(0)
      performanceSamples.clear()
      totalFramesProcessed.set(0)
      totalProcessingTime.set(0)
      minExecutionTime = Double.MAX_VALUE
      maxExecutionTime = Double.MIN_VALUE
    }
  }

  fun getSuggestedFrameRate(maxFrameRate: Double = 30.0): Double {
    if (!hasEnoughData) return maxFrameRate

    val avgTime = averageExecutionTimeSeconds
    if (avgTime <= 0) return maxFrameRate

    // Calculate theoretical max FPS based on processing time
    val theoreticalMaxFps = 1.0 / avgTime

    // Add safety margin (20%) to prevent frame drops
    val safeMaxFps = theoreticalMaxFps * 0.8

    // Clamp to reasonable bounds
    return min(safeMaxFps, maxFrameRate).coerceAtLeast(1.0)
  }
}
