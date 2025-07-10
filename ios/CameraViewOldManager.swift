//
//  CameraViewOldManager.swift
//  mrousavy
//
//  Created by Marc Rousavy on 09.11.20.
//  Copyright © 2020 mrousavy. All rights reserved.
//

import AVFoundation
import Foundation

@objc(CameraViewOldManager)
final class CameraViewOldManager: RCTViewManager {
  // pragma MARK: Properties

  private var moduleImplementation: CameraViewOldModuleImpl?
  private var viewImplementation: CameraViewOldManagerImpl?

  override var bridge: RCTBridge! {
    didSet {
      // Initialize shared implementations
      self.moduleImplementation = CameraViewOldModuleImpl(bridge: self.bridge)
      self.viewImplementation = CameraViewOldManagerImpl(bridge: self.bridge)
    }
  }

  override var methodQueue: DispatchQueue! {
    return DispatchQueue.main
  }

  override static func requiresMainQueueSetup() -> Bool {
    return true
  }

  override final func view() -> UIView! {
    return viewImplementation?.createView() ?? CameraViewOld()
  }

  // pragma MARK: React Functions

  @objc
  final func startRecording(_ node: NSNumber, options: NSDictionary, onRecordCallback: @escaping RCTResponseSenderBlock) {
    moduleImplementation?.startRecording(node.doubleValue, options: options, onRecordCallback: onRecordCallback)
  }

  @objc
  final func pauseRecording(_ node: NSNumber, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    moduleImplementation?.pauseRecording(node.doubleValue, resolve: resolve, reject: reject)
  }

  @objc
  final func resumeRecording(_ node: NSNumber, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    moduleImplementation?.resumeRecording(node.doubleValue, resolve: resolve, reject: reject)
  }

  @objc
  final func stopRecording(_ node: NSNumber, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    moduleImplementation?.stopRecording(node.doubleValue, resolve: resolve, reject: reject)
  }

  @objc
  final func takePhoto(_ node: NSNumber, options: NSDictionary, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    moduleImplementation?.takePhoto(node.doubleValue, options: options, resolve: resolve, reject: reject)
  }

  @objc
  final func focus(_ node: NSNumber, point: NSDictionary, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    moduleImplementation?.focus(node.doubleValue, point: point, resolve: resolve, reject: reject)
  }

  @objc
  final func getAvailableVideoCodecs(_ node: NSNumber, fileType: String?, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    moduleImplementation?.getAvailableVideoCodecs(node.doubleValue, fileType: fileType, resolve: resolve, reject: reject)
  }

  @objc
  final func getAvailableCameraDevices(_ resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    moduleImplementation?.getAvailableCameraDevices(resolve, reject: reject)
  }

  @objc
  final func getCameraPermissionStatus(_ resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    withPromise(resolve: resolve, reject: reject) {
      return moduleImplementation?.getCameraPermissionStatus() ?? "not-determined"
    }
  }

  @objc
  final func getMicrophonePermissionStatus(_ resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    withPromise(resolve: resolve, reject: reject) {
      return moduleImplementation?.getMicrophonePermissionStatus() ?? "not-determined"
    }
  }

  @objc
  final func requestCameraPermission(_ resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    moduleImplementation?.requestCameraPermission(resolve, reject: reject)
  }

  @objc
  final func requestMicrophonePermission(_ resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    moduleImplementation?.requestMicrophonePermission(resolve, reject: reject)
  }

}
