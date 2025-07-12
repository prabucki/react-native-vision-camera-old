//
//  PhotoCaptureDelegate.swift
//  mrousavy
//
//  Created by Marc Rousavy on 15.12.20.
//  Copyright © 2020 mrousavy. All rights reserved.
//

import AVFoundation
import UIKit

private var delegatesReferences: [NSObject] = []

// MARK: - PhotoCaptureDelegate

class PhotoCaptureDelegate: NSObject, AVCapturePhotoCaptureDelegate {
  private let promise: Promise
  private let enableShutterSound: Bool

  required init(promise: Promise, enableShutterSound: Bool = true) {
    self.promise = promise
    self.enableShutterSound = enableShutterSound
    super.init()
    delegatesReferences.append(self)
  }

  func photoOutput(_: AVCapturePhotoOutput, didFinishProcessingPhoto photo: AVCapturePhoto, error: Error?) {
    defer {
      delegatesReferences.removeAll(where: { $0 == self })
    }

    if let error = error as NSError? {
      ReactLogger.log(level: .error, message: "Photo capture failed: \(error.localizedDescription)")
      promise.reject(error: .capture(.unknown(message: error.description)), cause: error)
      return
    }

    // Performance optimization: process photo data asynchronously
    DispatchQueue.global(qos: .userInitiated).async {
      self.processPhotoData(photo)
    }
  }

  private func processPhotoData(_ photo: AVCapturePhoto) {
    let startTime = DispatchTime.now()

    do {
      // Create temporary file with optimized path
      guard let tempFilePath = self.createOptimizedTempFile() else {
        promise.reject(error: .capture(.createTempFileError))
        return
      }

      let url = URL(fileURLWithPath: tempFilePath)

      // Get photo data efficiently
      guard let data = photo.fileDataRepresentation() else {
        ReactLogger.log(level: .error, message: "Failed to get photo data representation")
        promise.reject(error: .capture(.fileError))
        return
      }

      // Write data with optimized I/O
      try self.writePhotoDataOptimized(data: data, to: url)

      // Extract metadata efficiently
      let metadata = self.extractMetadataOptimized(from: photo)

      let endTime = DispatchTime.now()
      let duration = Double(endTime.uptimeNanoseconds - startTime.uptimeNanoseconds) / 1_000_000
      ReactLogger.log(level: .info, message: "Photo processing completed in \(duration)ms")

      // Return to main queue for promise resolution
      DispatchQueue.main.async {
        self.promise.resolve([
          "path": tempFilePath,
          "width": metadata["width"] ?? 0,
          "height": metadata["height"] ?? 0,
          "isRawPhoto": photo.isRawPhoto,
          "metadata": metadata["exif"] ?? [:],
          "thumbnail": photo.embeddedThumbnailPhotoFormat as Any,
        ])
      }

    } catch {
      ReactLogger.log(level: .error, message: "Photo processing error: \(error.localizedDescription)")
      DispatchQueue.main.async {
        self.promise.reject(error: .capture(.fileError), cause: error as NSError)
      }
    }
  }

  private func createOptimizedTempFile() -> String? {
    let error = ErrorPointer(nilLiteral: ())
    return RCTTempFilePath("jpeg", error)
  }

  private func writePhotoDataOptimized(data: Data, to url: URL) throws {
    // Use optimized writing with proper error handling
    try data.write(to: url, options: [.atomic])
  }

  private func extractMetadataOptimized(from photo: AVCapturePhoto) -> [String: Any] {
    var result: [String: Any] = [:]

    // Extract dimensions from EXIF data efficiently
    if let exif = photo.metadata["{Exif}"] as? [String: Any] {
      result["width"] = exif["PixelXDimension"] ?? 0
      result["height"] = exif["PixelYDimension"] ?? 0
      result["exif"] = photo.metadata
    } else {
      // Fallback to photo properties
      result["width"] = photo.resolvedSettings.photoDimensions.width
      result["height"] = photo.resolvedSettings.photoDimensions.height
      result["exif"] = photo.metadata
    }

    return result
  }

  func photoOutput(_: AVCapturePhotoOutput, didFinishCaptureFor _: AVCaptureResolvedPhotoSettings, error: Error?) {
    defer {
      delegatesReferences.removeAll(where: { $0 == self })
    }

    if let error = error as NSError? {
      ReactLogger.log(level: .error, message: "Photo capture completion failed: \(error.localizedDescription)")
      promise.reject(error: .capture(.unknown(message: error.description)), cause: error)
      return
    }
  }
}
