package com.mrousavy.old.camera

import android.view.View
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp

class CameraViewOldManagerImpl(private val reactContext: ReactApplicationContext) {

  private val cameraViewTransactions = mutableMapOf<CameraViewOld, ArrayList<String>>()

  fun createViewInstance(context: ThemedReactContext): CameraViewOld {
    val cameraViewModule = context.getNativeModule(CameraViewOldModule::class.java)
      ?: context.getNativeModule(CameraViewOldTurboModule::class.java)

    return if (cameraViewModule is CameraViewOldModule) {
      CameraViewOld(context, cameraViewModule.moduleImpl.frameProcessorThread)
    } else {
      val turboModule = cameraViewModule as CameraViewOldTurboModule
      CameraViewOld(context, turboModule.moduleImpl.frameProcessorThread)
    }
  }

  fun onAfterUpdateTransaction(view: CameraViewOld) {
    val changedProps = cameraViewTransactions[view] ?: ArrayList()
    view.update(changedProps)
    cameraViewTransactions.remove(view)
  }

  fun getExportedCustomDirectEventTypeConstants(): Map<String, Any>? {
    return mapOf(
      "cameraViewReady" to mapOf("registrationName" to "onViewReady"),
      "cameraInitialized" to mapOf("registrationName" to "onInitialized"),
      "cameraError" to mapOf("registrationName" to "onError"),
      "cameraPerformanceSuggestionAvailable" to mapOf("registrationName" to "onFrameProcessorPerformanceSuggestionAvailable")
    )
  }

  private fun addChangedPropToTransaction(view: CameraViewOld, changedProp: String) {
    if (cameraViewTransactions[view] == null) {
      cameraViewTransactions[view] = ArrayList()
    }
    cameraViewTransactions[view]!!.add(changedProp)
  }

  fun setCameraId(view: CameraViewOld, cameraId: String?) {
    if (view.cameraId != cameraId) {
      addChangedPropToTransaction(view, "cameraId")
    }
    view.cameraId = cameraId
  }

  fun setIsActive(view: CameraViewOld, isActive: Boolean) {
    if (view.isActive != isActive) {
      addChangedPropToTransaction(view, "isActive")
    }
    view.isActive = isActive
  }

  fun setPhoto(view: CameraViewOld, photo: Boolean?) {
    if (view.photo != photo) {
      addChangedPropToTransaction(view, "photo")
    }
    view.photo = photo
  }

  fun setVideo(view: CameraViewOld, video: Boolean?) {
    if (view.video != video) {
      addChangedPropToTransaction(view, "video")
    }
    view.video = video
  }

  fun setAudio(view: CameraViewOld, audio: Boolean?) {
    if (view.audio != audio) {
      addChangedPropToTransaction(view, "audio")
    }
    view.audio = audio
  }

  fun setEnableFrameProcessor(view: CameraViewOld, enableFrameProcessor: Boolean) {
    if (view.enableFrameProcessor != enableFrameProcessor) {
      addChangedPropToTransaction(view, "enableFrameProcessor")
    }
    view.enableFrameProcessor = enableFrameProcessor
  }

  fun setFrameProcessorFps(view: CameraViewOld, frameProcessorFps: Double) {
    if (view.frameProcessorFps != frameProcessorFps) {
      addChangedPropToTransaction(view, "frameProcessorFps")
    }
    view.frameProcessorFps = frameProcessorFps
  }

  fun setFormat(view: CameraViewOld, format: ReadableMap?) {
    if (view.format != format) {
      addChangedPropToTransaction(view, "format")
    }
    view.format = format
  }

  fun setFps(view: CameraViewOld, fps: Int) {
    if (view.fps != fps) {
      addChangedPropToTransaction(view, "fps")
    }
    view.fps = fps
  }

  fun setHdr(view: CameraViewOld, hdr: Boolean?) {
    if (view.hdr != hdr) {
      addChangedPropToTransaction(view, "hdr")
    }
    view.hdr = hdr
  }

  fun setLowLightBoost(view: CameraViewOld, lowLightBoost: Boolean?) {
    if (view.lowLightBoost != lowLightBoost) {
      addChangedPropToTransaction(view, "lowLightBoost")
    }
    view.lowLightBoost = lowLightBoost
  }

  fun setColorSpace(view: CameraViewOld, colorSpace: String?) {
    if (view.colorSpace != colorSpace) {
      addChangedPropToTransaction(view, "colorSpace")
    }
    view.colorSpace = colorSpace
  }

  fun setVideoStabilizationMode(view: CameraViewOld, videoStabilizationMode: String?) {
    if (view.videoStabilizationMode != videoStabilizationMode) {
      addChangedPropToTransaction(view, "videoStabilizationMode")
    }
    view.videoStabilizationMode = videoStabilizationMode
  }

  fun setPreset(view: CameraViewOld, preset: String?) {
    if (view.preset != preset) {
      addChangedPropToTransaction(view, "preset")
    }
    view.preset = preset
  }

  fun setTorch(view: CameraViewOld, torch: String?) {
    if (view.torch != torch) {
      addChangedPropToTransaction(view, "torch")
    }
    view.torch = torch
  }

  fun setZoom(view: CameraViewOld, zoom: Double) {
    if (view.zoom != zoom) {
      addChangedPropToTransaction(view, "zoom")
    }
    view.zoom = zoom
  }

  fun setEnableDepthData(view: CameraViewOld, enableDepthData: Boolean) {
    if (view.enableDepthData != enableDepthData) {
      addChangedPropToTransaction(view, "enableDepthData")
    }
    view.enableDepthData = enableDepthData
  }

  fun setEnableHighQualityPhotos(view: CameraViewOld, enableHighQualityPhotos: Boolean?) {
    if (view.enableHighQualityPhotos != enableHighQualityPhotos) {
      addChangedPropToTransaction(view, "enableHighQualityPhotos")
    }
    view.enableHighQualityPhotos = enableHighQualityPhotos
  }

  fun setEnablePortraitEffectsMatteDelivery(view: CameraViewOld, enablePortraitEffectsMatteDelivery: Boolean) {
    if (view.enablePortraitEffectsMatteDelivery != enablePortraitEffectsMatteDelivery) {
      addChangedPropToTransaction(view, "enablePortraitEffectsMatteDelivery")
    }
    view.enablePortraitEffectsMatteDelivery = enablePortraitEffectsMatteDelivery
  }

  fun setEnableZoomGesture(view: CameraViewOld, enableZoomGesture: Boolean) {
    if (view.enableZoomGesture != enableZoomGesture) {
      addChangedPropToTransaction(view, "enableZoomGesture")
    }
    view.enableZoomGesture = enableZoomGesture
  }

  fun setOrientation(view: CameraViewOld, orientation: String?) {
    if (view.orientation != orientation) {
      addChangedPropToTransaction(view, "orientation")
    }
    view.orientation = orientation
  }
}
