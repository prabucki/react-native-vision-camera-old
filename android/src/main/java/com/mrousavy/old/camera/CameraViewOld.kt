package com.mrousavy.old.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.hardware.camera2.*
import android.util.Log
import android.util.Range
import android.view.*
import android.view.View.OnTouchListener
import android.widget.FrameLayout
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.core.*
import androidx.camera.core.impl.*
import androidx.camera.extensions.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.video.VideoCapture
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.facebook.jni.HybridData
import com.facebook.proguard.annotations.DoNotStrip
import com.facebook.react.bridge.*
import com.facebook.react.uimanager.events.RCTEventEmitter
import com.mrousavy.old.camera.frameprocessor.FrameProcessorPerformanceDataCollector
import com.mrousavy.old.camera.frameprocessor.FrameProcessorRuntimeManagerOld
import com.mrousavy.old.camera.utils.*
import kotlinx.coroutines.*
import kotlinx.coroutines.guava.await
import java.lang.IllegalArgumentException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

//
// TODOs for the CameraViewOld which are currently too hard to implement either because of CameraX' limitations, or my brain capacity.
//
// CameraViewOld
// TODO: Actually use correct sizes for video and photo (currently it's both the video size)
// TODO: Configurable FPS higher than 30
// TODO: High-speed video recordings (export in CameraViewOldModule::getAvailableVideoDevices(), and set in CameraViewOld::configurePreview()) (120FPS+)
// TODO: configureSession() enableDepthData
// TODO: configureSession() enableHighQualityPhotos
// TODO: configureSession() enablePortraitEffectsMatteDelivery
// TODO: configureSession() colorSpace

// CameraViewOld+RecordVideo
// TODO: Better startRecording()/stopRecording() (promise + callback, wait for TurboModules/JSI)
// TODO: videoStabilizationMode
// TODO: Return Video size/duration

// CameraViewOld+TakePhoto
// TODO: Mirror selfie images
// TODO: takePhoto() depth data
// TODO: takePhoto() raw capture
// TODO: takePhoto() photoCodec ("hevc" | "jpeg" | "raw")
// TODO: takePhoto() qualityPrioritization
// TODO: takePhoto() enableAutoRedEyeReduction
// TODO: takePhoto() enableAutoStabilization
// TODO: takePhoto() enableAutoDistortionCorrection
// TODO: takePhoto() return with jsi::Value Image reference for faster capture

@Suppress("KotlinJniMissingFunction") // I use fbjni, Android Studio is not smart enough to realize that.
@SuppressLint("ClickableViewAccessibility", "ViewConstructor")
class CameraViewOld(context: Context, private val frameProcessorThread: ExecutorService) : FrameLayout(context), LifecycleOwner {
  companion object {
    const val TAG = "CameraViewOld"
    const val TAG_PERF = "CameraViewOld.performance"

    private val propsThatRequireSessionReconfiguration = arrayListOf("cameraId", "format", "fps", "hdr", "lowLightBoost", "photo", "video", "enableFrameProcessor")
    private val arrayListOfZoom = arrayListOf("zoom")
  }

  // react properties
  // props that require reconfiguring
  var cameraId: String? = null // this is actually not a react prop directly, but the result of setting device={}
  var enableDepthData = false
  var enableHighQualityPhotos: Boolean? = null
  var enablePortraitEffectsMatteDelivery = false
  // use-cases
  var photo: Boolean? = null
  var video: Boolean? = null
  var audio: Boolean? = null
  var enableFrameProcessor = false
  // props that require format reconfiguring
  var format: ReadableMap? = null
  var fps: Int? = null
  var hdr: Boolean? = null // nullable bool
  var colorSpace: String? = null
  var lowLightBoost: Boolean? = null // nullable bool
  // other props
  var isActive = false
  var torch = "off"
  var zoom: Float = 1f // in "factor"
  var orientation: String? = null
  var enableZoomGesture = false
    set(value) {
      field = value
      setOnTouchListener(if (value) touchEventListener else null)
    }
  var frameProcessorFps = 1.0
    set(value) {
      field = value
      actualFrameProcessorFps = if (value == -1.0) 30.0 else value
      lastFrameProcessorPerformanceEvaluation = System.currentTimeMillis()
      frameProcessorPerformanceDataCollector.clear()
    }

  // private properties
  private var isMounted = false
  private val reactContext: ReactContext
    get() = context as ReactContext

  @Suppress("JoinDeclarationAndAssignment")
  internal val previewView: PreviewView
  private val cameraExecutor = Executors.newSingleThreadExecutor()
  internal val takePhotoExecutor = Executors.newSingleThreadExecutor()
  internal val recordVideoExecutor = Executors.newSingleThreadExecutor()
  internal var coroutineScope = CoroutineScope(Dispatchers.Main)

  internal var camera: Camera? = null
  internal var imageCapture: ImageCapture? = null
  internal var videoCapture: VideoCapture<Recorder>? = null
  private var imageAnalysis: ImageAnalysis? = null
  private var preview: Preview? = null

  internal var activeVideoRecording: Recording? = null

  private var lastFrameProcessorCall = System.currentTimeMillis()

  private var extensionsManager: ExtensionsManager? = null

  private val scaleGestureListener: ScaleGestureDetector.SimpleOnScaleGestureListener
  private val scaleGestureDetector: ScaleGestureDetector
  private val touchEventListener: OnTouchListener

  private val lifecycleRegistry: LifecycleRegistry
  private var hostLifecycleState: Lifecycle.State

  private val inputRotation: Int
    get() {
      return context.displayRotation
    }
  private val outputRotation: Int
    get() {
      if (orientation != null) {
        // user is overriding output orientation
        return when (orientation!!) {
          "portrait" -> Surface.ROTATION_0
          "landscapeRight" -> Surface.ROTATION_90
          "portraitUpsideDown" -> Surface.ROTATION_180
          "landscapeLeft" -> Surface.ROTATION_270
          else -> throw InvalidTypeScriptUnionError("orientation", orientation!!)
        }
      } else {
        // use same as input rotation
        return inputRotation
      }
    }

  private var minZoom: Float = 1f
  private var maxZoom: Float = 1f

  private var actualFrameProcessorFps = 30.0
  private val frameProcessorPerformanceDataCollector = FrameProcessorPerformanceDataCollector()
  private var lastSuggestedFrameProcessorFps = 0.0
  private var lastFrameProcessorPerformanceEvaluation = System.currentTimeMillis()
  private val isReadyForNewEvaluation: Boolean
    get() {
      val lastPerformanceEvaluationElapsedTime = System.currentTimeMillis() - lastFrameProcessorPerformanceEvaluation
      return lastPerformanceEvaluationElapsedTime > 1000
    }

  @DoNotStrip
  private var mHybridData: HybridData? = null

  @Suppress("LiftReturnOrAssignment", "RedundantIf")
  internal val fallbackToSnapshot: Boolean
    @SuppressLint("UnsafeOptInUsageError")
    get() {
      if (video != true && !enableFrameProcessor) {
        // Both use-cases are disabled, so `photo` is the only use-case anyways. Don't need to fallback here.
        return false
      }
      cameraId?.let { cameraId ->
        val cameraManger = reactContext.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
        cameraManger?.let {
          val characteristics = cameraManger.getCameraCharacteristics(cameraId)
          val hardwareLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
          if (hardwareLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
            // Camera only supports a single use-case at a time
            return true
          } else {
            if (video == true && enableFrameProcessor) {
              // Camera supports max. 2 use-cases, but both are occupied by `frameProcessor` and `video`
              return true
            } else {
              // Camera supports max. 2 use-cases and only one is occupied (either `frameProcessor` or `video`), so we can add `photo`
              return false
            }
          }
        }
      }
      return false
    }

  init {
    if (FrameProcessorRuntimeManagerOld.enableFrameProcessors) {
      mHybridData = initHybrid()
    }

    previewView = PreviewView(context)
    previewView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    previewView.installHierarchyFitter() // If this is not called correctly, view finder will be black/blank
    addView(previewView)

    scaleGestureListener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
      override fun onScale(detector: ScaleGestureDetector): Boolean {
        zoom = max(min((zoom * detector.scaleFactor), maxZoom), minZoom)
        update(arrayListOfZoom)
        return true
      }
    }
    scaleGestureDetector = ScaleGestureDetector(context, scaleGestureListener)
    touchEventListener = OnTouchListener { _, event -> return@OnTouchListener scaleGestureDetector.onTouchEvent(event) }

    hostLifecycleState = Lifecycle.State.INITIALIZED
    lifecycleRegistry = LifecycleRegistry(this)
    reactContext.addLifecycleEventListener(object : LifecycleEventListener {
      override fun onHostResume() {
        hostLifecycleState = Lifecycle.State.RESUMED
        updateLifecycleState()
        // workaround for https://issuetracker.google.com/issues/147354615, preview must be bound on resume
        update(propsThatRequireSessionReconfiguration)
      }
      override fun onHostPause() {
        hostLifecycleState = Lifecycle.State.CREATED
        updateLifecycleState()
      }
      override fun onHostDestroy() {
        hostLifecycleState = Lifecycle.State.DESTROYED
        updateLifecycleState()
        cameraExecutor.shutdown()
        takePhotoExecutor.shutdown()
        recordVideoExecutor.shutdown()
        reactContext.removeLifecycleEventListener(this)
      }
    })
  }

  override fun onConfigurationChanged(newConfig: Configuration?) {
    super.onConfigurationChanged(newConfig)
    updateOrientation()
  }

  @SuppressLint("RestrictedApi")
  private fun updateOrientation() {
    preview?.targetRotation = inputRotation
    imageCapture?.targetRotation = outputRotation
    videoCapture?.targetRotation = outputRotation
    imageAnalysis?.targetRotation = outputRotation
  }

  private external fun initHybrid(): HybridData
  private external fun frameProcessorCallback(frame: ImageProxy)

  override val lifecycle: Lifecycle
    get() = lifecycleRegistry

  /**
   * Updates the custom Lifecycle to match the host activity's lifecycle, and if it's active we narrow it down to the [isActive] and [isAttachedToWindow] fields.
   */
  private fun updateLifecycleState() {
    val lifecycleBefore = lifecycleRegistry.currentState
    if (hostLifecycleState == Lifecycle.State.RESUMED) {
      // Host Lifecycle (Activity) is currently active (RESUMED), so we narrow it down to the view's lifecycle
      if (isActive && isAttachedToWindow) {
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
      } else {
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
      }
    } else {
      // Host Lifecycle (Activity) is currently inactive (STARTED or DESTROYED), so that overrules our view's lifecycle
      lifecycleRegistry.currentState = hostLifecycleState
    }
    Log.d(TAG, "Lifecycle went from ${lifecycleBefore.name} -> ${lifecycleRegistry.currentState.name} (isActive: $isActive | isAttachedToWindow: $isAttachedToWindow)")
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    updateLifecycleState()
    if (!isMounted) {
      isMounted = true
      invokeOnViewReady()
    }
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    updateLifecycleState()
  }

  /**
   * Invalidate all React Props and reconfigure the device
   */
  fun update(changedProps: ArrayList<String>) = previewView.post {
    // TODO: Does this introduce too much overhead?
    //  I need to .post on the previewView because it might've not been initialized yet
    //  I need to use CoroutineScope.launch because of the suspend fun [configureSession]
    coroutineScope.launch {
      try {
        val shouldReconfigureSession = changedProps.containsAny(propsThatRequireSessionReconfiguration)
        val shouldReconfigureZoom = shouldReconfigureSession || changedProps.contains("zoom")
        val shouldReconfigureTorch = shouldReconfigureSession || changedProps.contains("torch")
        val shouldUpdateOrientation = shouldReconfigureSession ||  changedProps.contains("orientation")

        if (changedProps.contains("isActive")) {
          updateLifecycleState()
        }
        if (shouldReconfigureSession) {
          configureSession()
        }
        if (shouldReconfigureZoom) {
          val zoomClamped = max(min(zoom, maxZoom), minZoom)
          camera!!.cameraControl.setZoomRatio(zoomClamped)
        }
        if (shouldReconfigureTorch) {
          camera!!.cameraControl.enableTorch(torch == "on")
        }
        if (shouldUpdateOrientation) {
          updateOrientation()
        }
      } catch (e: Throwable) {
        Log.e(TAG, "update() threw: ${e.message}")
        invokeOnError(e)
      }
    }
  }

  /**
   * Configures the camera capture session. This should only be called when the camera device changes.
   */
  @SuppressLint("UnsafeOptInUsageError")
  private fun configureSession() {
    try {
      val startTime = System.currentTimeMillis()
      Log.i(TAG, "Configuring Camera Session...")

      // Early validation to avoid unnecessary work
      if (cameraId == null) {
        Log.w(TAG, "Camera ID is null, skipping session configuration")
        return
      }

      val cameraProvider = ProcessCameraProvider.getInstance(reactContext).get()

      // Unbind previous use cases more efficiently
      try {
        cameraProvider.unbindAll()
      } catch (e: Exception) {
        Log.w(TAG, "Error unbinding previous use cases: ${e.message}")
      }

      val cameraSelector = CameraSelector.Builder().requireLensFacing(
        when (cameraId) {
          "front" -> CameraSelector.LENS_FACING_FRONT
          "back" -> CameraSelector.LENS_FACING_BACK
          else -> {
            Log.e(TAG, "Invalid camera ID: $cameraId")
            return
          }
        }
      ).build()

      val useCases = mutableListOf<UseCase>()
      val rotation = outputRotation

      // Configure preview with optimizations
      val previewBuilder = Preview.Builder()
        .setTargetRotation(rotation)

      // Apply format configuration if available
      format?.let { formatMap ->
        try {
          val width = formatMap.getInt("photoWidth")
          val height = formatMap.getInt("photoHeight")
          if (width > 0 && height > 0) {
            previewBuilder.setTargetResolution(Size(width, height))
          }
        } catch (e: Exception) {
          Log.w(TAG, "Error applying format configuration: ${e.message}")
        }
      }

      // Configure photo capture if enabled
      if (photo == true) {
        Log.i(TAG, "Adding ImageCapture use-case...")
        val imageCaptureBuilder = ImageCapture.Builder()
          .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
          .setTargetRotation(rotation)
          .setFlashMode(ImageCapture.FLASH_MODE_AUTO)

        // Apply format configuration
        format?.let { formatMap ->
          try {
            val width = formatMap.getInt("photoWidth")
            val height = formatMap.getInt("photoHeight")
            if (width > 0 && height > 0) {
              imageCaptureBuilder.setTargetResolution(Size(width, height))
            }
          } catch (e: Exception) {
            Log.w(TAG, "Error applying photo format: ${e.message}")
          }
        }

        imageCapture = imageCaptureBuilder.build()
        useCases.add(imageCapture!!)
      }

      // Configure video capture if enabled
      if (video == true) {
        Log.i(TAG, "Adding VideoCapture use-case...")

        val recorder = Recorder.Builder()
          .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
          .build()

        val videoCaptureBuilder = VideoCapture.Builder(recorder)
          .setTargetRotation(rotation)

        // Apply format configuration
        format?.let { formatMap ->
          try {
            val width = formatMap.getInt("videoWidth")
            val height = formatMap.getInt("videoHeight")
            if (width > 0 && height > 0) {
              videoCaptureBuilder.setTargetResolution(Size(width, height))
            }
          } catch (e: Exception) {
            Log.w(TAG, "Error applying video format: ${e.message}")
          }
        }

        videoCapture = videoCaptureBuilder.build()
        useCases.add(videoCapture!!)
      }

      // Configure image analysis for frame processing
      if (enableFrameProcessor) {
        Log.i(TAG, "Adding ImageAnalysis use-case...")
        val imageAnalysisBuilder = ImageAnalysis.Builder()
          .setTargetRotation(rotation)
          .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
          .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)

        // Use lower resolution for frame processing to improve performance
        val processingSize = Size(1280, 720) // 720p for processing
        imageAnalysisBuilder.setTargetResolution(processingSize)

        imageAnalysis = imageAnalysisBuilder.build().apply {
          setAnalyzer(cameraExecutor, { image ->
            // Early check to avoid unnecessary work
            if (!isActive || frameProcessorFps <= 0) {
              image.close()
              return@setAnalyzer
            }

            val now = System.nanoTime()
            val intervalNs = if (actualFrameProcessorFps > 0) {
              (1_000_000_000.0 / actualFrameProcessorFps).toLong()
            } else {
              33_333_333L // Default to ~30 FPS
            }

            val timeSinceLastCall = now - lastFrameProcessorCall

            if (timeSinceLastCall >= intervalNs) {
              // Check if we're already processing a frame to avoid backlog
              if (frameProcessorThread.isShutdown || frameProcessorThread.isTerminated) {
                image.close()
                return@setAnalyzer
              }

              lastFrameProcessorCall = now

              // Submit to frame processor thread with proper error handling
              frameProcessorThread.execute {
                var perfSample: PerformanceSampleCollection? = null
                try {
                  perfSample = frameProcessorPerformanceDataCollector.beginPerformanceSampleCollection()
                  frameProcessorCallback(image)
                } catch (e: Exception) {
                  Log.e(TAG, "Frame processor error: ${e.message}", e)
                } finally {
                  perfSample?.endPerformanceSampleCollection()

                  // Ensure image is always closed
                  try {
                    image.close()
                  } catch (e: Exception) {
                    Log.w(TAG, "Error closing image: ${e.message}")
                  }
                }
              }

              // Check for performance evaluation less frequently
              if (isReadyForNewEvaluation) {
                evaluateNewPerformanceSamples()
              }
            } else {
              // Frame dropped due to throttling
              image.close()
            }
          })
        }
        useCases.add(imageAnalysis!!)
      }

      // Build and configure preview
      preview = previewBuilder.build()
      Log.i(TAG, "Attaching ${useCases.size} use-cases...")
      preview!!.setSurfaceProvider(previewView.surfaceProvider)

      // Bind use cases to camera with error handling
      try {
        camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, *useCases.toTypedArray())

        // Update zoom constraints
        camera?.let { cam ->
          val zoomState = cam.cameraInfo.zoomState.value
          minZoom = zoomState?.minZoomRatio ?: 1f
          maxZoom = zoomState?.maxZoomRatio ?: 1f

          // Apply current zoom if different from default
          if (zoom != 1f) {
            val clampedZoom = zoom.coerceIn(minZoom, maxZoom)
            cam.cameraControl.setZoomRatio(clampedZoom)
          }
        }

        val duration = System.currentTimeMillis() - startTime
        Log.i(TAG_PERF, "Session configured in $duration ms! Camera: ${camera!!}")
        invokeOnInitialized()

      } catch (e: Exception) {
        Log.e(TAG, "Failed to bind use cases: ${e.message}", e)
        throw when (e) {
          is IllegalArgumentException -> {
            if (e.message?.contains("too many use cases") == true) {
              ParallelVideoProcessingNotSupportedError(e)
            } else {
              InvalidCameraDeviceError(e)
            }
          }
          else -> UnknownCameraError(e)
        }
      }

    } catch (exc: Throwable) {
      Log.e(TAG, "Failed to configure session: ${exc.message}", exc)
      throw when (exc) {
        is CameraError -> exc
        is IllegalArgumentException -> {
          if (exc.message?.contains("too many use cases") == true) {
            ParallelVideoProcessingNotSupportedError(exc)
          } else {
            InvalidCameraDeviceError(exc)
          }
        }
        else -> UnknownCameraError(exc)
      }
    }
  }

  private fun evaluateNewPerformanceSamples() {
    lastFrameProcessorPerformanceEvaluation = System.currentTimeMillis()

    if (!frameProcessorPerformanceDataCollector.hasEnoughData) {
      return
    }

    val suggestedFrameProcessorFps = frameProcessorPerformanceDataCollector.getSuggestedFrameRate(30.0)

    if (frameProcessorFps == -1.0) {
      // frameProcessorFps="auto" - automatically adjust based on performance
      val newFps = suggestedFrameProcessorFps.coerceIn(1.0, 30.0)
      if (Math.abs(actualFrameProcessorFps - newFps) > 0.5) {
        actualFrameProcessorFps = newFps
        Log.d(TAG_PERF, "Auto-adjusted frame processor FPS to: $actualFrameProcessorFps")
      }
    } else {
      // frameProcessorFps={someCustomFpsValue} - provide suggestions
      val currentFps = frameProcessorFps
      val difference = Math.abs(suggestedFrameProcessorFps - currentFps)

      // Only suggest if there's a significant difference (> 2 FPS)
      if (difference > 2.0 && suggestedFrameProcessorFps != lastSuggestedFrameProcessorFps) {
        invokeOnFrameProcessorPerformanceSuggestionAvailable(currentFps, suggestedFrameProcessorFps)
        lastSuggestedFrameProcessorFps = suggestedFrameProcessorFps

        // Log performance stats for debugging
        val stats = frameProcessorPerformanceDataCollector.processingStats
        Log.d(TAG_PERF, "Frame processor performance stats: $stats")
      }
    }
  }
}
