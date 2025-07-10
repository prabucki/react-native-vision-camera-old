//
//  CameraViewOldModuleImpl.swift
//  VisionCameraOld
//
//  Created by Marc Rousavy on 09.11.20.
//  Copyright © 2020 mrousavy. All rights reserved.
//

import AVFoundation
import Foundation

@objc
public class CameraViewOldModuleImpl: NSObject {
  private weak var bridge: RCTBridge?

  @objc
  public init(bridge: RCTBridge) {
    self.bridge = bridge
    super.init()
  }

  // MARK: - Permission Methods

  @objc
  public func getCameraPermissionStatus() -> String {
    let status = AVCaptureDevice.authorizationStatus(for: .video)
    return status.descriptor
  }

  @objc
  public func getMicrophonePermissionStatus() -> String {
    let status = AVCaptureDevice.authorizationStatus(for: .audio)
    return status.descriptor
  }

  @objc
  public func requestCameraPermission(_ resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    AVCaptureDevice.requestAccess(for: .video) { granted in
      let result: AVAuthorizationStatus = granted ? .authorized : .denied
      resolve(result.descriptor)
    }
  }

  @objc
  public func requestMicrophonePermission(_ resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    AVCaptureDevice.requestAccess(for: .audio) { granted in
      let result: AVAuthorizationStatus = granted ? .authorized : .denied
      resolve(result.descriptor)
    }
  }

  // MARK: - Device Methods

  @objc
  public func getAvailableCameraDevices(_ resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    withPromise(resolve: resolve, reject: reject) {
      let discoverySession = AVCaptureDevice.DiscoverySession(deviceTypes: getAllDeviceTypes(), mediaType: .video, position: .unspecified)
      let devices = discoverySession.devices.filter {
        if #available(iOS 11.1, *) {
          // exclude the true-depth camera. The True-Depth camera has YUV and Infrared, can't take photos!
          return $0.deviceType != .builtInTrueDepthCamera
        }
        return true
      }
      return devices.map {
        return [
          "id": $0.uniqueID,
          "devices": $0.physicalDevices.map(\.deviceType.descriptor),
          "position": $0.position.descriptor,
          "name": $0.localizedName,
          "hasFlash": $0.hasFlash,
          "hasTorch": $0.hasTorch,
          "minZoom": $0.minAvailableVideoZoomFactor,
          "neutralZoom": $0.neutralZoomFactor,
          "maxZoom": $0.maxAvailableVideoZoomFactor,
          "isMultiCam": $0.isMultiCam,
          "supportsParallelVideoProcessing": true,
          "supportsDepthCapture": false, // TODO: supportsDepthCapture
          "supportsRawCapture": false, // TODO: supportsRawCapture
          "supportsLowLightBoost": $0.isLowLightBoostSupported,
          "supportsFocus": $0.isFocusPointOfInterestSupported,
          "formats": $0.formats.map { format -> [String: Any] in
            format.toDictionary()
          },
        ]
      }
    }
  }

  // MARK: - Camera Operations

  @objc
  public func startRecording(_ viewTag: Double, options: NSDictionary, onRecordCallback: @escaping RCTResponseSenderBlock) {
    let component = getCameraViewOld(withTag: NSNumber(value: viewTag))
    component.startRecording(options: options, callback: onRecordCallback)
  }

  @objc
  public func pauseRecording(_ viewTag: Double, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    let component = getCameraViewOld(withTag: NSNumber(value: viewTag))
    component.pauseRecording(promise: Promise(resolver: resolve, rejecter: reject))
  }

  @objc
  public func resumeRecording(_ viewTag: Double, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    let component = getCameraViewOld(withTag: NSNumber(value: viewTag))
    component.resumeRecording(promise: Promise(resolver: resolve, rejecter: reject))
  }

  @objc
  public func stopRecording(_ viewTag: Double, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    let component = getCameraViewOld(withTag: NSNumber(value: viewTag))
    component.stopRecording(promise: Promise(resolver: resolve, rejecter: reject))
  }

  @objc
  public func takePhoto(_ viewTag: Double, options: NSDictionary, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    let component = getCameraViewOld(withTag: NSNumber(value: viewTag))
    component.takePhoto(options: options, promise: Promise(resolver: resolve, rejecter: reject))
  }

  @objc
  public func focus(_ viewTag: Double, point: NSDictionary, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    let promise = Promise(resolver: resolve, rejecter: reject)
    guard let x = point["x"] as? NSNumber, let y = point["y"] as? NSNumber else {
      promise.reject(error: .parameter(.invalid(unionName: "point", receivedValue: point.description)))
      return
    }
    let component = getCameraViewOld(withTag: NSNumber(value: viewTag))
    component.focus(point: CGPoint(x: x.doubleValue, y: y.doubleValue), promise: promise)
  }

  @objc
  public func getAvailableVideoCodecs(_ viewTag: Double, fileType: String?, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    withPromise(resolve: resolve, reject: reject) {
      let component = getCameraViewOld(withTag: NSNumber(value: viewTag))
      guard let videoOutput = component.videoOutput else {
        throw CameraError.session(SessionError.cameraNotReady)
      }

      var parsedFileType = AVFileType.mov
      if fileType != nil {
        guard let parsed = try? AVFileType(withString: fileType!) else {
          throw CameraError.parameter(ParameterError.invalid(unionName: "fileType", receivedValue: fileType!))
        }
        parsedFileType = parsed
      }

      return videoOutput.availableVideoCodecTypesForAssetWriter(writingTo: parsedFileType).map(\.descriptor)
    }
  }

  // MARK: - Private Helper Methods

  private func getCameraViewOld(withTag tag: NSNumber) -> CameraViewOld {
    guard let bridge = bridge else {
      fatalError("Bridge is nil")
    }
    // swiftlint:disable force_cast
    return bridge.uiManager.view(forReactTag: tag) as! CameraViewOld
  }

  private func getAllDeviceTypes() -> [AVCaptureDevice.DeviceType] {
    var deviceTypes: [AVCaptureDevice.DeviceType] = []
    if #available(iOS 13.0, *) {
      deviceTypes.append(.builtInTripleCamera)
      deviceTypes.append(.builtInDualWideCamera)
      deviceTypes.append(.builtInUltraWideCamera)
    }
    if #available(iOS 11.1, *) {
      deviceTypes.append(.builtInTrueDepthCamera)
    }
    deviceTypes.append(.builtInDualCamera)
    deviceTypes.append(.builtInWideAngleCamera)
    deviceTypes.append(.builtInTelephotoCamera)
    return deviceTypes
  }
}
