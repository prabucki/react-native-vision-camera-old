//
//  CameraViewOld+AVCaptureSession.swift
//  VisionCameraOld
//
//  Created by Marc Rousavy on 26.03.21.
//  Copyright © 2021 mrousavy. All rights reserved.
//

import AVFoundation
import Foundation

/**
 Extension for CameraViewOld that sets up the AVCaptureSession, Device and Format.
 */
extension CameraViewOld {
  // pragma MARK: Configure Capture Session

  /**
   Configures the Capture Session.
   */
  final func configureCaptureSession() {
    ReactLogger.log(level: .info, message: "Configuring Camera Session...")

    guard let cameraId = cameraId else {
      ReactLogger.log(level: .error, message: "Camera ID is nil!")
      invokeOnError(.device(.noDevice))
      return
    }

    do {
      let startTime = DispatchTime.now()

      // Begin configuration with proper session management
      captureSession.beginConfiguration()
      defer {
        captureSession.commitConfiguration()
      }

      // Remove all existing inputs and outputs efficiently
      captureSession.inputs.forEach { input in
        captureSession.removeInput(input)
      }
      captureSession.outputs.forEach { output in
        captureSession.removeOutput(output)
      }

      // Configure camera device
      guard let videoDevice = AVCaptureDevice(uniqueID: cameraId as String) else {
        ReactLogger.log(level: .error, message: "Camera device not found for ID: \(cameraId)")
        invokeOnError(.device(.invalid))
        return
      }

      // Create and configure video input
      let videoDeviceInput = try AVCaptureDeviceInput(device: videoDevice)
      guard captureSession.canAddInput(videoDeviceInput) else {
        ReactLogger.log(level: .error, message: "Cannot add video input to session")
        invokeOnError(.session(.cameraNotReady))
        return
      }

      captureSession.addInput(videoDeviceInput)
      self.videoDeviceInput = videoDeviceInput

      // Configure audio input if needed
      if audio?.boolValue == true {
        do {
          try configureAudioCaptureSession()
        } catch {
          ReactLogger.log(level: .warning, message: "Failed to configure audio: \(error.localizedDescription)")
          // Don't fail the entire session for audio issues
        }
      }

      // Configure session preset for optimal performance
      if captureSession.canSetSessionPreset(.high) {
        captureSession.sessionPreset = .high
      }

      // Configure photo output if enabled
      if photo?.boolValue == true {
        ReactLogger.log(level: .info, message: "Adding Photo Output...")
        let photoOutput = AVCapturePhotoOutput()

        // Configure photo output settings
        photoOutput.isHighResolutionCaptureEnabled = enableHighQualityPhotos?.boolValue ?? false
        photoOutput.isDepthDataDeliveryEnabled = enableDepthData
        photoOutput.isPortraitEffectsMatteDeliveryEnabled = enablePortraitEffectsMatteDelivery

        guard captureSession.canAddOutput(photoOutput) else {
          ReactLogger.log(level: .error, message: "Cannot add photo output to session")
          invokeOnError(.session(.cameraNotReady))
          return
        }

        captureSession.addOutput(photoOutput)
        self.photoOutput = photoOutput
      }

      // Configure video output if enabled
      if video?.boolValue == true || enableFrameProcessor {
        ReactLogger.log(level: .info, message: "Adding Video Output...")
        let videoOutput = AVCaptureVideoDataOutput()

        // Configure video output settings for optimal performance
        videoOutput.videoSettings = [
          kCVPixelBufferPixelFormatTypeKey as String: kCVPixelFormatType_420YpCbCr8BiPlanarFullRange
        ]
        videoOutput.alwaysDiscardsLateVideoFrames = true

        // Use appropriate queue for video processing
        let videoQueue = enableFrameProcessor ? CameraQueues.frameProcessorQueue : CameraQueues.videoQueue
        videoOutput.setSampleBufferDelegate(self, queue: videoQueue)

        guard captureSession.canAddOutput(videoOutput) else {
          ReactLogger.log(level: .error, message: "Cannot add video output to session")
          invokeOnError(.session(.cameraNotReady))
          return
        }

        captureSession.addOutput(videoOutput)
        self.videoOutput = videoOutput

        // Configure video connection
        if let connection = videoOutput.connection(with: .video) {
          connection.isEnabled = true
          if connection.isVideoStabilizationSupported {
            connection.preferredVideoStabilizationMode = .auto
          }
        }
      }

      // Configure audio output if recording video with audio
      if video?.boolValue == true && audio?.boolValue == true {
        ReactLogger.log(level: .info, message: "Adding Audio Output...")
        let audioOutput = AVCaptureAudioDataOutput()
        audioOutput.setSampleBufferDelegate(self, queue: CameraQueues.audioQueue)

        if audioCaptureSession.canAddOutput(audioOutput) {
          audioCaptureSession.addOutput(audioOutput)
          self.audioOutput = audioOutput
        }
      }

      // Apply format configuration if specified
      try configureFormat()

      // Configure device-specific settings
      try configureDevice()

      let endTime = DispatchTime.now()
      let duration = Double(endTime.uptimeNanoseconds - startTime.uptimeNanoseconds) / 1_000_000
      ReactLogger.log(level: .info, message: "Session configured in \(duration)ms")

      // Notify that camera is ready
      invokeOnInitialized()

    } catch let error as CameraError {
      ReactLogger.log(level: .error, message: "Camera configuration failed: \(error.message)")
      invokeOnError(error)
    } catch {
      ReactLogger.log(level: .error, message: "Unexpected error during camera configuration: \(error.localizedDescription)")
      invokeOnError(.unknown(message: error.localizedDescription))
    }
  }

  // pragma MARK: Configure Device

  /**
   Configures the Video Device with the given FPS, HDR and ColorSpace.
   */
  final func configureDevice() {
    ReactLogger.log(level: .info, message: "Configuring Device...")
    guard let device = videoDeviceInput?.device else {
      invokeOnError(.session(.cameraNotReady))
      return
    }

    do {
      try device.lockForConfiguration()

      if let fps = fps?.int32Value {
        let supportsGivenFps = device.activeFormat.videoSupportedFrameRateRanges.contains { range in
          return range.includes(fps: Double(fps))
        }
        if !supportsGivenFps {
          invokeOnError(.format(.invalidFps(fps: Int(fps))))
          return
        }

        let duration = CMTimeMake(value: 1, timescale: fps)
        device.activeVideoMinFrameDuration = duration
        device.activeVideoMaxFrameDuration = duration
      } else {
        device.activeVideoMinFrameDuration = CMTime.invalid
        device.activeVideoMaxFrameDuration = CMTime.invalid
      }
      if hdr != nil {
        if hdr == true && !device.activeFormat.isVideoHDRSupported {
          invokeOnError(.format(.invalidHdr))
          return
        }
        if !device.automaticallyAdjustsVideoHDREnabled {
          if device.isVideoHDREnabled != hdr!.boolValue {
            device.isVideoHDREnabled = hdr!.boolValue
          }
        }
      }
      if lowLightBoost != nil {
        if lowLightBoost == true && !device.isLowLightBoostSupported {
          invokeOnError(.device(.lowLightBoostNotSupported))
          return
        }
        if device.automaticallyEnablesLowLightBoostWhenAvailable != lowLightBoost!.boolValue {
          device.automaticallyEnablesLowLightBoostWhenAvailable = lowLightBoost!.boolValue
        }
      }
      if let colorSpace = colorSpace as String? {
        guard let avColorSpace = try? AVCaptureColorSpace(string: colorSpace),
              device.activeFormat.supportedColorSpaces.contains(avColorSpace) else {
          invokeOnError(.format(.invalidColorSpace(colorSpace: colorSpace)))
          return
        }
        device.activeColorSpace = avColorSpace
      }

      device.unlockForConfiguration()
      ReactLogger.log(level: .info, message: "Device successfully configured!")
    } catch let error as NSError {
      invokeOnError(.device(.configureError), cause: error)
      return
    }
  }

  // pragma MARK: Configure Format

  /**
   Configures the Video Device to find the best matching Format.
   */
  final func configureFormat() {
    ReactLogger.log(level: .info, message: "Configuring Format...")
    guard let filter = format else {
      // Format Filter was null. Ignore it.
      return
    }
    guard let device = videoDeviceInput?.device else {
      invokeOnError(.session(.cameraNotReady))
      return
    }

    if device.activeFormat.matchesFilter(filter) {
      ReactLogger.log(level: .info, message: "Active format already matches filter.")
      return
    }

    // get matching format
    let matchingFormats = device.formats.filter { $0.matchesFilter(filter) }.sorted { $0.isBetterThan($1) }
    guard let format = matchingFormats.first else {
      invokeOnError(.format(.invalidFormat))
      return
    }

    do {
      try device.lockForConfiguration()
      device.activeFormat = format
      device.unlockForConfiguration()
      ReactLogger.log(level: .info, message: "Format successfully configured!")
    } catch let error as NSError {
      invokeOnError(.device(.configureError), cause: error)
      return
    }
  }

  // pragma MARK: Notifications/Interruptions

  @objc
  func sessionRuntimeError(notification: Notification) {
    ReactLogger.log(level: .error, message: "Unexpected Camera Runtime Error occured!")
    guard let error = notification.userInfo?[AVCaptureSessionErrorKey] as? AVError else {
      return
    }

    invokeOnError(.unknown(message: error._nsError.description), cause: error._nsError)

    if isActive {
      // restart capture session after an error occured
      cameraQueue.async {
        self.captureSession.startRunning()
      }
    }
  }
}
