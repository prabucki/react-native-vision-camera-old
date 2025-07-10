package com.mrousavy.old.camera

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewGroupManager
import com.facebook.react.uimanager.ThemedReactContext

@Suppress("unused")
class CameraViewOldFabricManager(reactContext: ReactApplicationContext) : ViewGroupManager<CameraViewOld>() {

  private val managerImpl = CameraViewOldManagerImpl(reactContext)

  public override fun createViewInstance(context: ThemedReactContext): CameraViewOld {
    return managerImpl.createViewInstance(context)
  }

  override fun onAfterUpdateTransaction(view: CameraViewOld) {
    super.onAfterUpdateTransaction(view)
    managerImpl.onAfterUpdateTransaction(view)
  }

  override fun getExportedCustomDirectEventTypeConstants(): MutableMap<String, Any>? {
    return managerImpl.getExportedCustomDirectEventTypeConstants()?.toMutableMap()
  }

  override fun getName(): String {
    return "CameraViewOld"
  }

  // Fabric Component Properties - delegated to implementation

  fun setCameraId(view: CameraViewOld, cameraId: String?) {
    managerImpl.setCameraId(view, cameraId)
  }

  fun setIsActive(view: CameraViewOld, isActive: Boolean) {
    managerImpl.setIsActive(view, isActive)
  }

  fun setPhoto(view: CameraViewOld, photo: Boolean?) {
    managerImpl.setPhoto(view, photo)
  }

  fun setVideo(view: CameraViewOld, video: Boolean?) {
    managerImpl.setVideo(view, video)
  }

  fun setAudio(view: CameraViewOld, audio: Boolean?) {
    managerImpl.setAudio(view, audio)
  }

  fun setEnableFrameProcessor(view: CameraViewOld, enableFrameProcessor: Boolean) {
    managerImpl.setEnableFrameProcessor(view, enableFrameProcessor)
  }

  fun setEnableDepthData(view: CameraViewOld, enableDepthData: Boolean) {
    managerImpl.setEnableDepthData(view, enableDepthData)
  }

  fun setEnableHighQualityPhotos(view: CameraViewOld, enableHighQualityPhotos: Boolean?) {
    managerImpl.setEnableHighQualityPhotos(view, enableHighQualityPhotos)
  }

  fun setEnablePortraitEffectsMatteDelivery(view: CameraViewOld, enablePortraitEffectsMatteDelivery: Boolean) {
    managerImpl.setEnablePortraitEffectsMatteDelivery(view, enablePortraitEffectsMatteDelivery)
  }

  fun setFormat(view: CameraViewOld, format: com.facebook.react.bridge.ReadableMap?) {
    managerImpl.setFormat(view, format)
  }

  fun setFps(view: CameraViewOld, fps: Int?) {
    managerImpl.setFps(view, fps)
  }

  fun setFrameProcessorFps(view: CameraViewOld, frameProcessorFps: Double) {
    managerImpl.setFrameProcessorFps(view, frameProcessorFps)
  }

  fun setHdr(view: CameraViewOld, hdr: Boolean?) {
    managerImpl.setHdr(view, hdr)
  }

  fun setLowLightBoost(view: CameraViewOld, lowLightBoost: Boolean?) {
    managerImpl.setLowLightBoost(view, lowLightBoost)
  }

  fun setColorSpace(view: CameraViewOld, colorSpace: String?) {
    managerImpl.setColorSpace(view, colorSpace)
  }

  fun setTorch(view: CameraViewOld, torch: String) {
    managerImpl.setTorch(view, torch)
  }

  fun setZoom(view: CameraViewOld, zoom: Float) {
    managerImpl.setZoom(view, zoom)
  }

  fun setOrientation(view: CameraViewOld, orientation: String?) {
    managerImpl.setOrientation(view, orientation)
  }

  fun setEnableZoomGesture(view: CameraViewOld, enableZoomGesture: Boolean) {
    managerImpl.setEnableZoomGesture(view, enableZoomGesture)
  }

  companion object {
    const val TAG = "CameraViewOld"
  }
}
