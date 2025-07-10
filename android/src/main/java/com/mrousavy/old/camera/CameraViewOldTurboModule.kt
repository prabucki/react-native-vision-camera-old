package com.mrousavy.old.camera

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableMap

class CameraViewOldTurboModule(reactContext: ReactApplicationContext) : NativeCameraViewOldSpec(reactContext) {

  private val moduleImpl = CameraViewOldModuleImpl(reactContext)

  override fun initialize() {
    super.initialize()
    moduleImpl.initialize()
  }

  override fun invalidate() {
    super.invalidate()
    moduleImpl.invalidate()
  }

  override fun getName(): String {
    return "CameraViewOld"
  }

  override fun getCameraPermissionStatus(promise: Promise) {
    moduleImpl.getCameraPermissionStatus(promise)
  }

  override fun getMicrophonePermissionStatus(promise: Promise) {
    moduleImpl.getMicrophonePermissionStatus(promise)
  }

  override fun requestCameraPermission(promise: Promise) {
    moduleImpl.requestCameraPermission(promise)
  }

  override fun requestMicrophonePermission(promise: Promise) {
    moduleImpl.requestMicrophonePermission(promise)
  }

  override fun getAvailableCameraDevices(promise: Promise) {
    moduleImpl.getAvailableCameraDevices(promise)
  }

  override fun takePhoto(viewTag: Double, options: ReadableMap, promise: Promise) {
    moduleImpl.takePhoto(viewTag.toInt(), options, promise)
  }

  override fun takeSnapshot(viewTag: Double, options: ReadableMap, promise: Promise) {
    moduleImpl.takeSnapshot(viewTag.toInt(), options, promise)
  }

  override fun startRecording(viewTag: Double, options: ReadableMap, promise: Promise) {
    moduleImpl.startRecording(viewTag.toInt(), options, promise)
  }

  override fun stopRecording(viewTag: Double, promise: Promise) {
    moduleImpl.stopRecording(viewTag.toInt(), promise)
  }

  override fun pauseRecording(viewTag: Double, promise: Promise) {
    moduleImpl.pauseRecording(viewTag.toInt(), promise)
  }

  override fun resumeRecording(viewTag: Double, promise: Promise) {
    moduleImpl.resumeRecording(viewTag.toInt(), promise)
  }

  override fun focus(viewTag: Double, point: ReadableMap, promise: Promise) {
    moduleImpl.focus(viewTag.toInt(), point, promise)
  }
}
