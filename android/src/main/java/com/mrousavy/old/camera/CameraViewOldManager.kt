package com.mrousavy.old.camera

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.common.MapBuilder
import com.facebook.react.uimanager.ViewGroupManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp

@Suppress("unused")
class CameraViewOldManager(reactContext: ReactApplicationContext) : ViewGroupManager<CameraViewOld>() {

  public override fun createViewInstance(context: ThemedReactContext): CameraViewOld {
    val cameraViewModule = context.getNativeModule(CameraViewOldModule::class.java)!!
    return CameraViewOld(context, cameraViewModule.frameProcessorThread)
  }

  override fun onAfterUpdateTransaction(view: CameraViewOld) {
    super.onAfterUpdateTransaction(view)
    val changedProps = cameraViewTransactions[view] ?: ArrayList()
    view.update(changedProps)
    cameraViewTransactions.remove(view)
  }

  override fun getExportedCustomDirectEventTypeConstants(): MutableMap<String, Any>? {
    return MapBuilder.builder<String, Any>()
      .put("cameraViewReady", MapBuilder.of("registrationName", "onViewReady"))
      .put("cameraInitialized", MapBuilder.of("registrationName", "onInitialized"))
      .put("cameraError", MapBuilder.of("registrationName", "onError"))
      .put("cameraPerformanceSuggestionAvailable", MapBuilder.of("registrationName", "onFrameProcessorPerformanceSuggestionAvailable"))
      .build()?.toMutableMap()
  }

  override fun getName(): String {
    return TAG
  }

  @ReactProp(name = "cameraId")
  fun setCameraId(view: CameraViewOld, cameraId: String) {
    if (view.cameraId != cameraId)
      addChangedPropToTransaction(view, "cameraId")
    view.cameraId = cameraId
  }

  @ReactProp(name = "photo")
  fun setPhoto(view: CameraViewOld, photo: Boolean?) {
    if (view.photo != photo)
      addChangedPropToTransaction(view, "photo")
    view.photo = photo
  }

  @ReactProp(name = "video")
  fun setVideo(view: CameraViewOld, video: Boolean?) {
    if (view.video != video)
      addChangedPropToTransaction(view, "video")
    view.video = video
  }

  @ReactProp(name = "audio")
  fun setAudio(view: CameraViewOld, audio: Boolean?) {
    if (view.audio != audio)
      addChangedPropToTransaction(view, "audio")
    view.audio = audio
  }

  @ReactProp(name = "enableFrameProcessor")
  fun setEnableFrameProcessor(view: CameraViewOld, enableFrameProcessor: Boolean) {
    if (view.enableFrameProcessor != enableFrameProcessor)
      addChangedPropToTransaction(view, "enableFrameProcessor")
    view.enableFrameProcessor = enableFrameProcessor
  }

  @ReactProp(name = "enableDepthData")
  fun setEnableDepthData(view: CameraViewOld, enableDepthData: Boolean) {
    if (view.enableDepthData != enableDepthData)
      addChangedPropToTransaction(view, "enableDepthData")
    view.enableDepthData = enableDepthData
  }

  @ReactProp(name = "enableHighQualityPhotos")
  fun setEnableHighQualityPhotos(view: CameraViewOld, enableHighQualityPhotos: Boolean?) {
    if (view.enableHighQualityPhotos != enableHighQualityPhotos)
      addChangedPropToTransaction(view, "enableHighQualityPhotos")
    view.enableHighQualityPhotos = enableHighQualityPhotos
  }

  @ReactProp(name = "enablePortraitEffectsMatteDelivery")
  fun setEnablePortraitEffectsMatteDelivery(view: CameraViewOld, enablePortraitEffectsMatteDelivery: Boolean) {
    if (view.enablePortraitEffectsMatteDelivery != enablePortraitEffectsMatteDelivery)
      addChangedPropToTransaction(view, "enablePortraitEffectsMatteDelivery")
    view.enablePortraitEffectsMatteDelivery = enablePortraitEffectsMatteDelivery
  }

  @ReactProp(name = "format")
  fun setFormat(view: CameraViewOld, format: ReadableMap?) {
    if (view.format != format)
      addChangedPropToTransaction(view, "format")
    view.format = format
  }

  // TODO: Change when TurboModules release.
  // We're treating -1 as "null" here, because when I make the fps parameter
  // of type "Int?" the react bridge throws an error.
  @ReactProp(name = "fps", defaultInt = -1)
  fun setFps(view: CameraViewOld, fps: Int) {
    if (view.fps != fps)
      addChangedPropToTransaction(view, "fps")
    view.fps = if (fps > 0) fps else null
  }

  @ReactProp(name = "frameProcessorFps", defaultDouble = 1.0)
  fun setFrameProcessorFps(view: CameraViewOld, frameProcessorFps: Double) {
    if (view.frameProcessorFps != frameProcessorFps)
      addChangedPropToTransaction(view, "frameProcessorFps")
    view.frameProcessorFps = frameProcessorFps
  }

  @ReactProp(name = "hdr")
  fun setHdr(view: CameraViewOld, hdr: Boolean?) {
    if (view.hdr != hdr)
      addChangedPropToTransaction(view, "hdr")
    view.hdr = hdr
  }

  @ReactProp(name = "lowLightBoost")
  fun setLowLightBoost(view: CameraViewOld, lowLightBoost: Boolean?) {
    if (view.lowLightBoost != lowLightBoost)
      addChangedPropToTransaction(view, "lowLightBoost")
    view.lowLightBoost = lowLightBoost
  }

  @ReactProp(name = "colorSpace")
  fun setColorSpace(view: CameraViewOld, colorSpace: String?) {
    if (view.colorSpace != colorSpace)
      addChangedPropToTransaction(view, "colorSpace")
    view.colorSpace = colorSpace
  }

  @ReactProp(name = "isActive")
  fun setIsActive(view: CameraViewOld, isActive: Boolean) {
    if (view.isActive != isActive)
      addChangedPropToTransaction(view, "isActive")
    view.isActive = isActive
  }

  @ReactProp(name = "torch")
  fun setTorch(view: CameraViewOld, torch: String) {
    if (view.torch != torch)
      addChangedPropToTransaction(view, "torch")
    view.torch = torch
  }

  @ReactProp(name = "zoom")
  fun setZoom(view: CameraViewOld, zoom: Double) {
    val zoomFloat = zoom.toFloat()
    if (view.zoom != zoomFloat)
      addChangedPropToTransaction(view, "zoom")
    view.zoom = zoomFloat
  }

  @ReactProp(name = "enableZoomGesture")
  fun setEnableZoomGesture(view: CameraViewOld, enableZoomGesture: Boolean) {
    if (view.enableZoomGesture != enableZoomGesture)
      addChangedPropToTransaction(view, "enableZoomGesture")
    view.enableZoomGesture = enableZoomGesture
  }

  @ReactProp(name = "orientation")
  fun setOrientation(view: CameraViewOld, orientation: String) {
    if (view.orientation != orientation)
      addChangedPropToTransaction(view, "orientation")
    view.orientation = orientation
  }

  companion object {
    const val TAG = "CameraViewOld"

    val cameraViewTransactions: HashMap<CameraViewOld, ArrayList<String>> = HashMap()

    private fun addChangedPropToTransaction(view: CameraViewOld, changedProp: String) {
      if (cameraViewTransactions[view] == null) {
        cameraViewTransactions[view] = ArrayList()
      }
      cameraViewTransactions[view]!!.add(changedProp)
    }
  }
}
