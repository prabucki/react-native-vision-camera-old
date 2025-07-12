//
// Created by Marc Rousavy on 14.06.21.
//

#include "CameraViewOld.h"

#include <jni.h>
#include <fbjni/fbjni.h>
#include <jsi/jsi.h>

#include <memory>
#include <string>
#include <regex>
#include <exception>
#include <stdexcept>

namespace vision {

using namespace facebook;
using namespace jni;

using TSelf = local_ref<CameraViewOld::jhybriddata>;

TSelf CameraViewOld::initHybrid(alias_ref<HybridClass::jhybridobject> jThis) {
    return makeCxxInstance(jThis);
}

void CameraViewOld::registerNatives() {
    registerHybrid({
        makeNativeMethod("initHybrid", CameraViewOld::initHybrid),
        makeNativeMethod("frameProcessorCallback", CameraViewOld::frameProcessorCallback),
    });
}

void CameraViewOld::frameProcessorCallback(const alias_ref<JImageProxy::javaobject>& frame) {
  // Early validation to prevent crashes
  if (!frame) {
    __android_log_write(ANDROID_LOG_ERROR, TAG, "Frame processor callback called with null frame!");
    return;
  }

  if (frameProcessor_ == nullptr) {
    __android_log_write(ANDROID_LOG_WARN, TAG, "Called Frame Processor callback, but `frameProcessor` is null!");
    return;
  }

  // Validate frame before processing
  try {
    // Check if frame is still valid
    static const auto isImageProxyValid = jni::findClassStatic("com/mrousavy/old/camera/frameprocessor/ImageProxyUtils")
        ->getStaticMethod<jboolean(jni::alias_ref<JImageProxy::javaobject>)>("isImageProxyValid");

    if (!isImageProxyValid(jni::findClassStatic("com/mrousavy/old/camera/frameprocessor/ImageProxyUtils"), frame)) {
      __android_log_write(ANDROID_LOG_WARN, TAG, "Frame processor called with invalid frame, skipping...");
      return;
    }
  } catch (const std::exception& e) {
    __android_log_print(ANDROID_LOG_ERROR, TAG, "Error validating frame: %s", e.what());
    return;
  }

  // Execute frame processor with comprehensive error handling
  try {
    frameProcessor_(frame);
  } catch (const jsi::JSError& error) {
    // Handle JavaScript errors gracefully
    try {
      auto message = error.getMessage();
      auto stack = error.getStack();
      auto formattedStack = std::regex_replace(stack, std::regex("\n"), "\n    ");
      __android_log_print(ANDROID_LOG_ERROR, TAG,
        "Frame Processor threw a JavaScript error!\nMessage: %s\nStack:\n    %s",
        message.c_str(), formattedStack.c_str());
    } catch (...) {
      __android_log_write(ANDROID_LOG_ERROR, TAG, "Frame Processor threw a JavaScript error (details unavailable)");
    }
  } catch (const std::runtime_error& error) {
    __android_log_print(ANDROID_LOG_ERROR, TAG, "Frame Processor threw a runtime error: %s", error.what());
  } catch (const std::invalid_argument& error) {
    __android_log_print(ANDROID_LOG_ERROR, TAG, "Frame Processor threw an invalid argument error: %s", error.what());
  } catch (const std::exception& error) {
    __android_log_print(ANDROID_LOG_ERROR, TAG, "Frame Processor threw a C++ exception: %s", error.what());
  } catch (...) {
    __android_log_write(ANDROID_LOG_ERROR, TAG, "Frame Processor threw an unknown exception!");

    // Try to recover by clearing the frame processor
    try {
      frameProcessor_ = nullptr;
      __android_log_write(ANDROID_LOG_WARN, TAG, "Frame processor cleared due to unknown exception");
    } catch (...) {
      // If we can't even clear the processor, log and continue
      __android_log_write(ANDROID_LOG_FATAL, TAG, "Critical error: Unable to clear frame processor after unknown exception");
    }
  }
}

void CameraViewOld::setFrameProcessor(const TFrameProcessor&& frameProcessor) {
  try {
    frameProcessor_ = frameProcessor;
    __android_log_write(ANDROID_LOG_INFO, TAG, "Frame processor set successfully");
  } catch (const std::exception& e) {
    __android_log_print(ANDROID_LOG_ERROR, TAG, "Error setting frame processor: %s", e.what());
  }
}

void CameraViewOld::unsetFrameProcessor() {
  try {
    frameProcessor_ = nullptr;
    __android_log_write(ANDROID_LOG_INFO, TAG, "Frame processor unset successfully");
  } catch (const std::exception& e) {
    __android_log_print(ANDROID_LOG_ERROR, TAG, "Error unsetting frame processor: %s", e.what());
  }
}

} // namespace vision
