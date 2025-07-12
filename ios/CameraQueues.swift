//
//  CameraQueues.swift
//  VisionCameraOld
//
//  Created by Marc Rousavy on 22.03.21.
//  Copyright © 2021 mrousavy. All rights reserved.
//

import Foundation

@objc
public class CameraQueues: NSObject {
  /// The serial execution queue for the camera preview layer (input stream) as well as output processing of photos.
  /// Uses high QoS for real-time camera operations
  @objc public static let cameraQueue = DispatchQueue(label: "mrousavy/VisionCameraOld.main",
                                                      qos: .userInteractive,
                                                      attributes: [],
                                                      autoreleaseFrequency: .workItem,
                                                      target: nil)

  /// The serial execution queue for output processing of videos for recording.
  /// Uses high QoS for smooth video recording
  @objc public static let videoQueue = DispatchQueue(label: "mrousavy/VisionCameraOld.video",
                                                     qos: .userInteractive,
                                                     attributes: [],
                                                     autoreleaseFrequency: .workItem,
                                                     target: nil)

  /// The concurrent execution queue for output processing of videos for frame processing.
  /// Uses utility QoS as frame processing can be more flexible with timing
  @objc public static let frameProcessorQueue = DispatchQueue(label: "mrousavy/VisionCameraOld.frame-processor",
                                                              qos: .utility,
                                                              attributes: .concurrent,
                                                              autoreleaseFrequency: .workItem,
                                                              target: nil)

  /// The serial execution queue for output processing of audio buffers.
  /// Uses user-initiated QoS for audio processing
  @objc public static let audioQueue = DispatchQueue(label: "mrousavy/VisionCameraOld.audio",
                                                     qos: .userInitiated,
                                                     attributes: [],
                                                     autoreleaseFrequency: .workItem,
                                                     target: nil)

  /// A high-priority queue for critical camera operations that need immediate attention
  @objc public static let priorityQueue = DispatchQueue(label: "mrousavy/VisionCameraOld.priority",
                                                        qos: .userInteractive,
                                                        attributes: [],
                                                        autoreleaseFrequency: .workItem,
                                                        target: nil)

  /// A background queue for non-critical operations like cleanup and maintenance
  @objc public static let backgroundQueue = DispatchQueue(label: "mrousavy/VisionCameraOld.background",
                                                          qos: .background,
                                                          attributes: .concurrent,
                                                          autoreleaseFrequency: .workItem,
                                                          target: nil)

  /// Thread-local storage for performance optimization
  private static let threadLocalStorage = ThreadLocal<[String: Any]>()

  /// Optimized execution helper that reduces queue switching overhead
  @objc public static func executeOnCameraQueue(block: @escaping () -> Void) {
    if DispatchQueue.getSpecific(key: cameraQueueKey) != nil {
      // Already on camera queue, execute immediately
      block()
    } else {
      cameraQueue.async(execute: block)
    }
  }

  /// Optimized execution helper for frame processor queue
  @objc public static func executeOnFrameProcessorQueue(block: @escaping () -> Void) {
    if DispatchQueue.getSpecific(key: frameProcessorQueueKey) != nil {
      // Already on frame processor queue, execute immediately
      block()
    } else {
      frameProcessorQueue.async(execute: block)
    }
  }

  /// Setup queue-specific keys for optimization
  private static let cameraQueueKey = DispatchSpecificKey<String>()
  private static let frameProcessorQueueKey = DispatchSpecificKey<String>()
  private static let videoQueueKey = DispatchSpecificKey<String>()
  private static let audioQueueKey = DispatchSpecificKey<String>()

  /// Initialize queue-specific identifiers
  @objc public static func setupQueueIdentifiers() {
    cameraQueue.setSpecific(key: cameraQueueKey, value: "camera")
    frameProcessorQueue.setSpecific(key: frameProcessorQueueKey, value: "frameProcessor")
    videoQueue.setSpecific(key: videoQueueKey, value: "video")
    audioQueue.setSpecific(key: audioQueueKey, value: "audio")
  }

  /// Check if currently executing on a specific queue
  @objc public static func isOnCameraQueue() -> Bool {
    return DispatchQueue.getSpecific(key: cameraQueueKey) != nil
  }

  @objc public static func isOnFrameProcessorQueue() -> Bool {
    return DispatchQueue.getSpecific(key: frameProcessorQueueKey) != nil
  }

  @objc public static func isOnVideoQueue() -> Bool {
    return DispatchQueue.getSpecific(key: videoQueueKey) != nil
  }

  @objc public static func isOnAudioQueue() -> Bool {
    return DispatchQueue.getSpecific(key: audioQueueKey) != nil
  }
}

/// Thread-local storage helper for performance optimization
private class ThreadLocal<T> {
  private let key = NSString(format: "ThreadLocal_%p", unsafeBitCast(self, to: Int.self))

  func get() -> T? {
    return Thread.current.threadDictionary[key] as? T
  }

  func set(_ value: T?) {
    if let value = value {
      Thread.current.threadDictionary[key] = value
    } else {
      Thread.current.threadDictionary.removeObject(forKey: key)
    }
  }
}
