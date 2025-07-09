package com.mrousavy.old.camera.utils

import com.facebook.react.bridge.Promise
import com.mrousavy.old.camera.CameraError
import com.mrousavy.old.camera.UnknownCameraError

inline fun withPromise(promise: Promise, closure: () -> Any?) {
  try {
    val result = closure()
    promise.resolve(result)
  } catch (e: Throwable) {
    e.printStackTrace()
    val error = if (e is CameraError) e else UnknownCameraError(e)
    promise.reject("${error.domain}/${error.id}", error.message, error.cause)
  }
}
