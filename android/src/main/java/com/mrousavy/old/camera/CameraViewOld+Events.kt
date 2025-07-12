package com.mrousavy.old.camera

import android.util.Log
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.events.RCTEventEmitter

fun CameraViewOld.invokeOnInitialized() {
  Log.i(CameraViewOld.TAG, "invokeOnInitialized()")

  try {
    val reactContext = context as ReactContext
    reactContext.getJSModule(RCTEventEmitter::class.java).receiveEvent(id, "cameraInitialized", null)
  } catch (e: Exception) {
    Log.e(CameraViewOld.TAG, "Error invoking onInitialized: ${e.message}", e)
  }
}

fun CameraViewOld.invokeOnError(error: Throwable) {
  Log.e(CameraViewOld.TAG, "invokeOnError(...):")
  error.printStackTrace()

  try {
    val cameraError = when (error) {
      is CameraError -> error
      else -> UnknownCameraError(error)
    }

    val event = Arguments.createMap()
    event.putString("code", cameraError.code)
    event.putString("message", cameraError.message)

    // Add additional error context
    event.putString("domain", cameraError.domain)
    event.putString("timestamp", System.currentTimeMillis().toString())

    cameraError.cause?.let { cause ->
      event.putMap("cause", errorToMap(cause))
    }

    val reactContext = context as ReactContext
    reactContext.getJSModule(RCTEventEmitter::class.java).receiveEvent(id, "cameraError", event)

    // Attempt error recovery for certain types of errors
    attemptErrorRecovery(cameraError)

  } catch (e: Exception) {
    Log.e(CameraViewOld.TAG, "Error invoking onError: ${e.message}", e)
    // Fallback: try to send a generic error event
    try {
      val fallbackEvent = Arguments.createMap()
      fallbackEvent.putString("code", "unknown/event-error")
      fallbackEvent.putString("message", "Failed to send error event: ${e.message}")
      val reactContext = context as ReactContext
      reactContext.getJSModule(RCTEventEmitter::class.java).receiveEvent(id, "cameraError", fallbackEvent)
    } catch (fallbackError: Exception) {
      Log.e(CameraViewOld.TAG, "Critical error: Cannot send error events: ${fallbackError.message}", fallbackError)
    }
  }
}

fun CameraViewOld.invokeOnFrameProcessorPerformanceSuggestionAvailable(currentFps: Double, suggestedFps: Double) {
  Log.d(CameraViewOld.TAG, "invokeOnFrameProcessorPerformanceSuggestionAvailable(suggestedFps: $suggestedFps)")

  try {
    val event = Arguments.createMap()
    val type = if (suggestedFps > currentFps) "can-use-higher-fps" else "should-use-lower-fps"
    event.putString("type", type)
    event.putDouble("suggestedFrameProcessorFps", suggestedFps)
    event.putDouble("currentFrameProcessorFps", currentFps)

    val reactContext = context as ReactContext
    reactContext.getJSModule(RCTEventEmitter::class.java).receiveEvent(id, "cameraPerformanceSuggestionAvailable", event)
  } catch (e: Exception) {
    Log.e(CameraViewOld.TAG, "Error invoking onFrameProcessorPerformanceSuggestionAvailable: ${e.message}", e)
  }
}

fun CameraViewOld.invokeOnViewReady() {
  try {
    val event = Arguments.createMap()
    val reactContext = context as ReactContext
    reactContext.getJSModule(RCTEventEmitter::class.java).receiveEvent(id, "cameraViewReady", event)
  } catch (e: Exception) {
    Log.e(CameraViewOld.TAG, "Error invoking onViewReady: ${e.message}", e)
  }
}

private fun CameraViewOld.attemptErrorRecovery(error: CameraError) {
  Log.d(CameraViewOld.TAG, "Attempting error recovery for: ${error.code}")

  when (error.code) {
    "session/camera-not-ready" -> {
      // Try to reconfigure the session after a delay
      coroutineScope.launch {
        try {
          kotlinx.coroutines.delay(1000) // Wait 1 second
          if (isActive) {
            Log.i(CameraViewOld.TAG, "Attempting to reconfigure camera session after error")
            configureSession()
          }
        } catch (e: Exception) {
          Log.e(CameraViewOld.TAG, "Error recovery failed: ${e.message}", e)
        }
      }
    }

    "device/invalid-device" -> {
      // Try to fallback to a different camera if available
      coroutineScope.launch {
        try {
          kotlinx.coroutines.delay(500)
          val fallbackCameraId = if (cameraId == "front") "back" else "front"
          Log.i(CameraViewOld.TAG, "Attempting to fallback to camera: $fallbackCameraId")
          // Note: This would require additional logic to actually switch cameras
        } catch (e: Exception) {
          Log.e(CameraViewOld.TAG, "Camera fallback failed: ${e.message}", e)
        }
      }
    }

    "capture/file-io-error" -> {
      // Clear any cached files and try to free up space
      try {
        val cacheDir = context.cacheDir
        cacheDir.listFiles()?.forEach { file ->
          if (file.name.startsWith("mrousavy") && file.lastModified() < System.currentTimeMillis() - 300000) { // 5 minutes old
            file.delete()
            Log.d(CameraViewOld.TAG, "Cleaned up old cache file: ${file.name}")
          }
        }
      } catch (e: Exception) {
        Log.w(CameraViewOld.TAG, "Error cleaning cache: ${e.message}")
      }
    }

    else -> {
      Log.d(CameraViewOld.TAG, "No specific recovery strategy for error: ${error.code}")
    }
  }
}

private fun errorToMap(error: Throwable): WritableMap {
  val map = Arguments.createMap()

  try {
    map.putString("message", error.message ?: "Unknown error")
    map.putString("stacktrace", error.stackTraceToString())

    // Add cause if available
    error.cause?.let { cause ->
      map.putMap("cause", errorToMap(cause))
    }

  } catch (e: Exception) {
    Log.w(CameraViewOld.TAG, "Error creating error map: ${e.message}")
    map.putString("message", "Error details unavailable")
  }

  return map
}
