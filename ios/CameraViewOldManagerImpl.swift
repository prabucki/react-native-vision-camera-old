//
//  CameraViewOldManagerImpl.swift
//  VisionCameraOld
//
//  Created by Marc Rousavy on 09.11.20.
//  Copyright © 2020 mrousavy. All rights reserved.
//

import AVFoundation
import Foundation

@objc
public class CameraViewOldManagerImpl: NSObject {
  private weak var bridge: RCTBridge?
  private var runtimeManager: FrameProcessorRuntimeManagerOld?

  @objc
  public init(bridge: RCTBridge) {
    self.bridge = bridge
    super.init()

    // Install Frame Processor bindings and setup Runtime
    if VISION_CAMERA_ENABLE_FRAME_PROCESSORS {
      CameraQueues.frameProcessorQueue.async {
        self.runtimeManager = FrameProcessorRuntimeManagerOld(bridge: bridge)
        bridge.runOnJS {
          self.runtimeManager!.installFrameProcessorBindings()
        }
      }
    }
  }

  @objc
  public func createView() -> UIView {
    return CameraViewOld()
  }

  // MARK: - Property Setters

  @objc
  public func setIsActive(_ view: CameraViewOld, value: Bool) {
    view.isActive = value
  }

  @objc
  public func setCameraId(_ view: CameraViewOld, value: String?) {
    view.cameraId = value as NSString?
  }

  @objc
  public func setEnableDepthData(_ view: CameraViewOld, value: Bool) {
    view.enableDepthData = value
  }

  @objc
  public func setEnableHighQualityPhotos(_ view: CameraViewOld, value: NSNumber?) {
    view.enableHighQualityPhotos = value
  }

  @objc
  public func setEnablePortraitEffectsMatteDelivery(_ view: CameraViewOld, value: Bool) {
    view.enablePortraitEffectsMatteDelivery = value
  }

  @objc
  public func setPhoto(_ view: CameraViewOld, value: NSNumber?) {
    view.photo = value
  }

  @objc
  public func setVideo(_ view: CameraViewOld, value: NSNumber?) {
    view.video = value
  }

  @objc
  public func setAudio(_ view: CameraViewOld, value: NSNumber?) {
    view.audio = value
  }

  @objc
  public func setEnableFrameProcessor(_ view: CameraViewOld, value: Bool) {
    view.enableFrameProcessor = value
  }

  @objc
  public func setFormat(_ view: CameraViewOld, value: NSDictionary?) {
    view.format = value
  }

  @objc
  public func setFps(_ view: CameraViewOld, value: NSNumber?) {
    view.fps = value
  }

  @objc
  public func setFrameProcessorFps(_ view: CameraViewOld, value: NSNumber) {
    view.frameProcessorFps = value
  }

  @objc
  public func setHdr(_ view: CameraViewOld, value: NSNumber?) {
    view.hdr = value
  }

  @objc
  public func setLowLightBoost(_ view: CameraViewOld, value: NSNumber?) {
    view.lowLightBoost = value
  }

  @objc
  public func setColorSpace(_ view: CameraViewOld, value: String?) {
    view.colorSpace = value as NSString?
  }

  @objc
  public func setVideoStabilizationMode(_ view: CameraViewOld, value: String?) {
    view.videoStabilizationMode = value as NSString?
  }

  @objc
  public func setPreset(_ view: CameraViewOld, value: String?) {
    view.preset = value
  }

  @objc
  public func setTorch(_ view: CameraViewOld, value: String) {
    view.torch = value
  }

  @objc
  public func setZoom(_ view: CameraViewOld, value: NSNumber) {
    view.zoom = value
  }

  @objc
  public func setEnableZoomGesture(_ view: CameraViewOld, value: Bool) {
    view.enableZoomGesture = value
  }

  @objc
  public func setOrientation(_ view: CameraViewOld, value: String?) {
    view.orientation = value as NSString?
  }

  // MARK: - Event Handlers

  @objc
  public func setOnError(_ view: CameraViewOld, value: RCTDirectEventBlock?) {
    view.onError = value
  }

  @objc
  public func setOnInitialized(_ view: CameraViewOld, value: RCTDirectEventBlock?) {
    view.onInitialized = value
  }

  @objc
  public func setOnFrameProcessorPerformanceSuggestionAvailable(_ view: CameraViewOld, value: RCTDirectEventBlock?) {
    view.onFrameProcessorPerformanceSuggestionAvailable = value
  }

  @objc
  public func setOnViewReady(_ view: CameraViewOld, value: RCTDirectEventBlock?) {
    view.onViewReady = value
  }
}
