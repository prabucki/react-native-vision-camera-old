package com.mrousavy.old.camera.frameprocessor

import android.util.Log
import androidx.annotation.Keep
import com.facebook.jni.HybridData
import com.facebook.proguard.annotations.DoNotStrip
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.common.annotations.FrameworkAPI
import com.facebook.react.turbomodule.core.CallInvokerHolderImpl
import com.facebook.react.uimanager.UIManagerHelper
import com.mrousavy.old.camera.CameraViewOld
import com.mrousavy.old.camera.ViewNotFoundError
import java.lang.ref.WeakReference
import java.util.concurrent.ExecutorService

@OptIn(FrameworkAPI::class)
@Suppress("KotlinJniMissingFunction") // I use fbjni, Android Studio is not smart enough to realize that.
class FrameProcessorRuntimeManagerOld(context: ReactApplicationContext, frameProcessorThread: ExecutorService) {
  companion object {
    const val TAG = "FrameProcessorRuntime"
    val Plugins: ArrayList<FrameProcessorPlugin> = ArrayList()
    var enableFrameProcessors = true

    init {
      try {
        System.loadLibrary("reanimated")
        System.loadLibrary("VisionCameraOld")
      } catch (e: UnsatisfiedLinkError) {
        Log.w(TAG, "Failed to load Reanimated/VisionCameraOld C++ library. Frame Processors are disabled!")
        enableFrameProcessors = false
      }
    }
  }

  @DoNotStrip
  private var mHybridData: HybridData? = null
  private var mContext: WeakReference<ReactApplicationContext>? = null
  private var mScheduler: VisionCameraOldScheduler? = null

  init {
    if (enableFrameProcessors) {
      val holder = context.catalystInstance.jsCallInvokerHolder as CallInvokerHolderImpl
      val jsRuntimeHolder =
        context.javaScriptContextHolder?.get() ?: throw Error("JSI Runtime is null! VisionCameraOld does not yet support bridgeless mode..")
      mScheduler = VisionCameraOldScheduler(frameProcessorThread)
      mContext = WeakReference(context)
      mHybridData = initHybrid(jsRuntimeHolder, holder, mScheduler!!)
      initializeRuntime()

      Log.i(TAG, "Installing JSI Bindings on JS Thread...")
      context.runOnJSQueueThread {
        installJSIBindings()
      }
    }
  }

  @Suppress("unused")
  @DoNotStrip
  @Keep
  fun findCameraViewOldById(viewId: Int): CameraViewOld {
    Log.d(TAG, "Finding view $viewId...")
    val ctx = mContext?.get()
    val view = if (ctx != null) UIManagerHelper.getUIManager(ctx, viewId)?.resolveView(viewId) as CameraViewOld? else null
    Log.d(TAG,  if (view != null) "Found view $viewId!" else "Couldn't find view $viewId!")
    return view ?: throw ViewNotFoundError(viewId)
  }

  @Suppress("unused")
  @DoNotStrip
  @Keep
  fun registerPlugins() {
    Log.i(TAG, "Installing Frame Processor Plugins...")
    Plugins.forEach { plugin ->
      registerPlugin(plugin)
    }
    Log.i(TAG, "Successfully installed ${Plugins.count()} Frame Processor Plugins!")
  }

  // private C++ funcs
  private external fun initHybrid(
    jsContext: Long,
    jsCallInvokerHolder: CallInvokerHolderImpl,
    scheduler: VisionCameraOldScheduler
  ): HybridData
  private external fun initializeRuntime()
  private external fun registerPlugin(plugin: FrameProcessorPlugin)
  private external fun installJSIBindings()
}
