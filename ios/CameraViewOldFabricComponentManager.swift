//
//  CameraViewOldFabricComponentManager.swift
//  VisionCameraOld
//
//  Created by Marc Rousavy on 09.11.20.
//  Copyright © 2020 mrousavy. All rights reserved.
//

import Foundation

@objc(CameraViewOldFabricComponentManager)
public class CameraViewOldFabricComponentManager: RCTViewComponentView {
  private let implementation: CameraViewOldManagerImpl

  @objc
  public init(bridge: RCTBridge) {
    self.implementation = CameraViewOldManagerImpl(bridge: bridge)
    super.init(frame: .zero)
  }

  @objc
  public override func view() -> UIView {
    return implementation.createView()
  }

  // MARK: - Fabric Component Properties

  @objc
  public func setIsActive(_ view: UIView, value: Bool) {
    if let cameraView = view as? CameraViewOld {
      implementation.setIsActive(cameraView, value: value)
    }
  }

  @objc
  public func setCameraId(_ view: UIView, value: String?) {
    if let cameraView = view as? CameraViewOld {
      implementation.setCameraId(cameraView, value: value)
    }
  }

  @objc
  public func setEnableDepthData(_ view: UIView, value: Bool) {
    if let cameraView = view as? CameraViewOld {
      implementation.setEnableDepthData(cameraView, value: value)
    }
  }

  @objc
  public func setEnableHighQualityPhotos(_ view: UIView, value: NSNumber?) {
    if let cameraView = view as? CameraViewOld {
      implementation.setEnableHighQualityPhotos(cameraView, value: value)
    }
  }

  @objc
  public func setEnablePortraitEffectsMatteDelivery(_ view: UIView, value: Bool) {
    if let cameraView = view as? CameraViewOld {
      implementation.setEnablePortraitEffectsMatteDelivery(cameraView, value: value)
    }
  }

  @objc
  public func setPhoto(_ view: UIView, value: NSNumber?) {
    if let cameraView = view as? CameraViewOld {
      implementation.setPhoto(cameraView, value: value)
    }
  }

  @objc
  public func setVideo(_ view: UIView, value: NSNumber?) {
    if let cameraView = view as? CameraViewOld {
      implementation.setVideo(cameraView, value: value)
    }
  }

  @objc
  public func setAudio(_ view: UIView, value: NSNumber?) {
    if let cameraView = view as? CameraViewOld {
      implementation.setAudio(cameraView, value: value)
    }
  }

  @objc
  public func setEnableFrameProcessor(_ view: UIView, value: Bool) {
    if let cameraView = view as? CameraViewOld {
      implementation.setEnableFrameProcessor(cameraView, value: value)
    }
  }

  @objc
  public func setFormat(_ view: UIView, value: NSDictionary?) {
    if let cameraView = view as? CameraViewOld {
      implementation.setFormat(cameraView, value: value)
    }
  }

  @objc
  public func setFps(_ view: UIView, value: NSNumber?) {
    if let cameraView = view as? CameraViewOld {
      implementation.setFps(cameraView, value: value)
    }
  }

  @objc
  public func setFrameProcessorFps(_ view: UIView, value: NSNumber) {
    if let cameraView = view as? CameraViewOld {
      implementation.setFrameProcessorFps(cameraView, value: value)
    }
  }

  @objc
  public func setHdr(_ view: UIView, value: NSNumber?) {
    if let cameraView = view as? CameraViewOld {
      implementation.setHdr(cameraView, value: value)
    }
  }

  @objc
  public func setLowLightBoost(_ view: UIView, value: NSNumber?) {
    if let cameraView = view as? CameraViewOld {
      implementation.setLowLightBoost(cameraView, value: value)
    }
  }

  @objc
  public func setColorSpace(_ view: UIView, value: String?) {
    if let cameraView = view as? CameraViewOld {
      implementation.setColorSpace(cameraView, value: value)
    }
  }

  @objc
  public func setVideoStabilizationMode(_ view: UIView, value: String?) {
    if let cameraView = view as? CameraViewOld {
      implementation.setVideoStabilizationMode(cameraView, value: value)
    }
  }

  @objc
  public func setPreset(_ view: UIView, value: String?) {
    if let cameraView = view as? CameraViewOld {
      implementation.setPreset(cameraView, value: value)
    }
  }

  @objc
  public func setTorch(_ view: UIView, value: String) {
    if let cameraView = view as? CameraViewOld {
      implementation.setTorch(cameraView, value: value)
    }
  }

  @objc
  public func setZoom(_ view: UIView, value: NSNumber) {
    if let cameraView = view as? CameraViewOld {
      implementation.setZoom(cameraView, value: value)
    }
  }

  @objc
  public func setEnableZoomGesture(_ view: UIView, value: Bool) {
    if let cameraView = view as? CameraViewOld {
      implementation.setEnableZoomGesture(cameraView, value: value)
    }
  }

  @objc
  public func setOrientation(_ view: UIView, value: String?) {
    if let cameraView = view as? CameraViewOld {
      implementation.setOrientation(cameraView, value: value)
    }
  }

  // MARK: - Event Handlers

  @objc
  public func setOnError(_ view: UIView, value: RCTDirectEventBlock?) {
    if let cameraView = view as? CameraViewOld {
      implementation.setOnError(cameraView, value: value)
    }
  }

  @objc
  public func setOnInitialized(_ view: UIView, value: RCTDirectEventBlock?) {
    if let cameraView = view as? CameraViewOld {
      implementation.setOnInitialized(cameraView, value: value)
    }
  }

  @objc
  public func setOnFrameProcessorPerformanceSuggestionAvailable(_ view: UIView, value: RCTDirectEventBlock?) {
    if let cameraView = view as? CameraViewOld {
      implementation.setOnFrameProcessorPerformanceSuggestionAvailable(cameraView, value: value)
    }
  }

  @objc
  public func setOnViewReady(_ view: UIView, value: RCTDirectEventBlock?) {
    if let cameraView = view as? CameraViewOld {
      implementation.setOnViewReady(cameraView, value: value)
    }
  }
}
