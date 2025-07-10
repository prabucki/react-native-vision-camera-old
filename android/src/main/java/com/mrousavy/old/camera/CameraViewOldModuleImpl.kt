package com.mrousavy.old.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.util.Log
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.extensions.ExtensionMode
import androidx.camera.extensions.ExtensionsManager
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.QualitySelector
import androidx.core.content.ContextCompat
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.PermissionAwareActivity
import com.facebook.react.modules.core.PermissionListener
import com.facebook.react.uimanager.UIManagerHelper
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.turbomodule.core.CallInvokerHolderImpl
import com.mrousavy.old.camera.CameraViewOld
import com.mrousavy.old.camera.ViewNotFoundError
import java.util.concurrent.ExecutorService
import com.mrousavy.old.camera.frameprocessor.FrameProcessorRuntimeManagerOld
import com.mrousavy.old.camera.parsers.*
import com.mrousavy.old.camera.utils.*
import kotlinx.coroutines.*
import kotlinx.coroutines.guava.await
import java.util.concurrent.Executors

class CameraViewOldModuleImpl(private val reactContext: ReactApplicationContext) {
  companion object {
    const val TAG = "CameraViewOld"
    var RequestCode = 10

    fun parsePermissionStatus(status: Int): String {
      return when (status) {
        PackageManager.PERMISSION_DENIED -> "denied"
        PackageManager.PERMISSION_GRANTED -> "authorized"
        else -> "not-determined"
      }
    }
  }

  var frameProcessorThread: ExecutorService = Executors.newSingleThreadExecutor()
  private val coroutineScope = CoroutineScope(Dispatchers.Default)
  private var frameProcessorManager: FrameProcessorRuntimeManagerOld? = null

  private fun cleanup() {
    if (coroutineScope.isActive) {
      coroutineScope.cancel("CameraViewOldModule has been destroyed.")
    }
    frameProcessorManager = null
  }

  fun initialize() {
    if (frameProcessorManager == null) {
      frameProcessorThread.execute {
        frameProcessorManager = FrameProcessorRuntimeManagerOld(reactContext, frameProcessorThread)
      }
    }
  }

  fun invalidate() {
    cleanup()
  }

  private fun findCameraViewOld(viewId: Int): CameraViewOld {
    Log.d(TAG, "Finding view $viewId...")
    val view = if (reactContext != null) UIManagerHelper.getUIManager(reactContext, viewId)?.resolveView(viewId) as CameraViewOld? else null
    Log.d(TAG,  if (reactContext != null) "Found view $viewId!" else "Couldn't find view $viewId!")
    return view ?: throw ViewNotFoundError(viewId)
  }

  fun getCameraPermissionStatus(promise: Promise) {
    val status = ContextCompat.checkSelfPermission(reactContext, Manifest.permission.CAMERA)
    promise.resolve(parsePermissionStatus(status))
  }

  fun getMicrophonePermissionStatus(promise: Promise) {
    val status = ContextCompat.checkSelfPermission(reactContext, Manifest.permission.RECORD_AUDIO)
    promise.resolve(parsePermissionStatus(status))
  }

  fun requestCameraPermission(promise: Promise) {
    val activity = reactContext.currentActivity as? PermissionAwareActivity
    if (activity == null) {
      promise.reject("NO_ACTIVITY", "No activity found! Make sure you call this method from a component that is currently mounted.")
      return
    }

    val listener = PermissionListener { requestCode, permissions, grantResults ->
      if (requestCode == RequestCode) {
        val permissionIndex = permissions.indexOf(Manifest.permission.CAMERA)
        val isGranted = grantResults.isNotEmpty() && grantResults[permissionIndex] == PackageManager.PERMISSION_GRANTED
        val status = if (isGranted) "authorized" else "denied"
        promise.resolve(status)
        return@PermissionListener true
      }
      return@PermissionListener false
    }

    activity.requestPermissions(arrayOf(Manifest.permission.CAMERA), RequestCode, listener)
  }

  fun requestMicrophonePermission(promise: Promise) {
    val activity = reactContext.currentActivity as? PermissionAwareActivity
    if (activity == null) {
      promise.reject("NO_ACTIVITY", "No activity found! Make sure you call this method from a component that is currently mounted.")
      return
    }

    val listener = PermissionListener { requestCode, permissions, grantResults ->
      if (requestCode == RequestCode + 1) {
        val permissionIndex = permissions.indexOf(Manifest.permission.RECORD_AUDIO)
        val isGranted = grantResults.isNotEmpty() && grantResults[permissionIndex] == PackageManager.PERMISSION_GRANTED
        val status = if (isGranted) "authorized" else "denied"
        promise.resolve(status)
        return@PermissionListener true
      }
      return@PermissionListener false
    }

    activity.requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), RequestCode + 1, listener)
  }

  fun takePhoto(viewTag: Int, options: ReadableMap, promise: Promise) {
    coroutineScope.launch {
      withPromise(promise) {
        val view = findCameraViewOld(viewTag)
        view.takePhoto(options)
      }
    }
  }

  fun takeSnapshot(viewTag: Int, options: ReadableMap, promise: Promise) {
    coroutineScope.launch {
      withPromise(promise) {
        val view = findCameraViewOld(viewTag)
        view.takeSnapshot(options)
      }
    }
  }

  fun startRecording(viewTag: Int, options: ReadableMap, promise: Promise) {
    coroutineScope.launch {
      withPromise(promise) {
        val view = findCameraViewOld(viewTag)
        view.startRecording(options) { video ->
          promise.resolve(video)
        }
        return@withPromise null
      }
    }
  }

  fun stopRecording(viewTag: Int, promise: Promise) {
    coroutineScope.launch {
      withPromise(promise) {
        val view = findCameraViewOld(viewTag)
        view.stopRecording()
        return@withPromise null
      }
    }
  }

  fun pauseRecording(viewTag: Int, promise: Promise) {
    coroutineScope.launch {
      withPromise(promise) {
        val view = findCameraViewOld(viewTag)
        view.pauseRecording()
        return@withPromise null
      }
    }
  }

  fun resumeRecording(viewTag: Int, promise: Promise) {
    coroutineScope.launch {
      withPromise(promise) {
        val view = findCameraViewOld(viewTag)
        view.resumeRecording()
        return@withPromise null
      }
    }
  }

  fun focus(viewTag: Int, point: ReadableMap, promise: Promise) {
    coroutineScope.launch {
      withPromise(promise) {
        val view = findCameraViewOld(viewTag)
        view.focus(point)
        return@withPromise null
      }
    }
  }

  fun getAvailableCameraDevices(promise: Promise) {
    // Delegate to the original implementation for now
    // This will be properly implemented in the TurboModule
    promise.resolve(Arguments.createArray())
  }
}
