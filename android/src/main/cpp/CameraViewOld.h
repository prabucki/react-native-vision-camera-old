//
// Created by Marc Rousavy on 14.06.21.
//

#pragma once

#include <jni.h>
#include <fbjni/fbjni.h>

#include <memory>

#include "java-bindings/JImageProxy.h"

namespace vision {

using namespace facebook;
using TFrameProcessor = std::function<void(jni::alias_ref<JImageProxy::javaobject>)>;

class CameraViewOld : public jni::HybridClass<CameraViewOld> {
 public:
  static auto constexpr kJavaDescriptor = "Lcom/mrousavy/old/camera/CameraViewOld;";
  static auto constexpr TAG = "VisionCameraOld";
  static jni::local_ref<jhybriddata> initHybrid(jni::alias_ref<jhybridobject> jThis);
  static void registerNatives();

  // TODO: Use template<> to avoid heap allocation for std::function<>
  void setFrameProcessor(const TFrameProcessor&& frameProcessor);
  void unsetFrameProcessor();

 private:
  friend HybridBase;
  jni::global_ref<CameraViewOld::javaobject> javaPart_;
  TFrameProcessor frameProcessor_;

  void frameProcessorCallback(const jni::alias_ref<JImageProxy::javaobject>& frame);

  explicit CameraViewOld(jni::alias_ref<CameraViewOld::jhybridobject> jThis) :
    javaPart_(jni::make_global(jThis)),
    frameProcessor_(nullptr)
  {}
};

} // namespace vision
