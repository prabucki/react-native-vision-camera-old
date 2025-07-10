//
//  CameraViewOldTurboModule.swift
//  VisionCameraOld
//
//  Created by Marc Rousavy on 09.11.20.
//  Copyright © 2020 mrousavy. All rights reserved.
//

import Foundation

@objc(CameraViewOldTurboModule)
public class CameraViewOldTurboModule: NSObject, NativeCameraViewOldSpec {
  private let implementation: CameraViewOldModuleImpl

  @objc
  public init(bridge: RCTBridge) {
    self.implementation = CameraViewOldModuleImpl(bridge: bridge)
    super.init()
  }

  public static func moduleName() -> String {
    return "CameraViewOld"
  }

  // MARK: - NativeCameraViewOldSpec Implementation

  public func getCameraPermissionStatus() -> String {
    return implementation.getCameraPermissionStatus()
  }

  public func getMicrophonePermissionStatus() -> String {
    return implementation.getMicrophonePermissionStatus()
  }

  public func requestCameraPermission(_ resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    implementation.requestCameraPermission(resolve, reject: reject)
  }

  public func requestMicrophonePermission(_ resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    implementation.requestMicrophonePermission(resolve, reject: reject)
  }

  public func getAvailableCameraDevices(_ resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    implementation.getAvailableCameraDevices(resolve, reject: reject)
  }

  public func startRecording(_ viewTag: Double, options: NSDictionary, onRecordCallback: @escaping RCTResponseSenderBlock) {
    implementation.startRecording(viewTag, options: options, onRecordCallback: onRecordCallback)
  }

  public func pauseRecording(_ viewTag: Double, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    implementation.pauseRecording(viewTag, resolve: resolve, reject: reject)
  }

  public func resumeRecording(_ viewTag: Double, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    implementation.resumeRecording(viewTag, resolve: resolve, reject: reject)
  }

  public func stopRecording(_ viewTag: Double, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    implementation.stopRecording(viewTag, resolve: resolve, reject: reject)
  }

  public func takePhoto(_ viewTag: Double, options: NSDictionary, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    implementation.takePhoto(viewTag, options: options, resolve: resolve, reject: reject)
  }

  public func focus(_ viewTag: Double, point: NSDictionary, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    implementation.focus(viewTag, point: point, resolve: resolve, reject: reject)
  }

  public func getAvailableVideoCodecs(_ viewTag: Double, fileType: String?, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    implementation.getAvailableVideoCodecs(viewTag, fileType: fileType, resolve: resolve, reject: reject)
  }
}
