package com.mrousavy.old.camera

import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager

class CameraPackageOld : ReactPackage {

  private fun isNewArchitectureEnabled(): Boolean {
    // Check if New Architecture is enabled
    // This can be determined by checking if TurboModule classes are available
    return try {
      Class.forName("com.facebook.react.turbomodule.core.TurboModule")
      // Additional check for the specific property
      val newArchEnabled = System.getProperty("newArchEnabled") == "true"
      newArchEnabled
    } catch (e: ClassNotFoundException) {
      false
    }
  }

  override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> {
    return if (isNewArchitectureEnabled()) {
      // New Architecture: Use TurboModule
      listOf(CameraViewOldTurboModule(reactContext))
    } else {
      // Old Architecture: Use traditional module
      listOf(CameraViewOldModule(reactContext))
    }
  }

  override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> {
    return if (isNewArchitectureEnabled()) {
      // New Architecture: Use Fabric Component Manager
      listOf(CameraViewOldFabricManager(reactContext))
    } else {
      // Old Architecture: Use traditional view manager
      listOf(CameraViewOldManager(reactContext))
    }
  }
}
