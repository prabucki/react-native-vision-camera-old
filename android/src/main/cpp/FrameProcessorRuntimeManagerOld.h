//
// Created by Marc Rousavy on 11.06.21.
//

#pragma once

#include <fbjni/fbjni.h>
#include <jsi/jsi.h>
#include <ReactCommon/CallInvokerHolder.h>
#include <memory>
#include <string>

#include "WorkletRuntime.h"

#include "CameraViewOld.h"
#include "VisionCameraOldScheduler.h"
#include "java-bindings/JFrameProcessorPlugin.h"

namespace vision {

using namespace facebook;

class FrameProcessorRuntimeManagerOld : public jni::HybridClass<FrameProcessorRuntimeManagerOld> {
 public:
  static auto constexpr kJavaDescriptor = "Lcom/mrousavy/old/camera/frameprocessor/FrameProcessorRuntimeManagerOld;";
  static auto constexpr TAG = "VisionCameraOld";
  static jni::local_ref<jhybriddata> initHybrid(jni::alias_ref<jhybridobject> jThis,
                                                jlong jsContext,
                                                jni::alias_ref<facebook::react::CallInvokerHolder::javaobject> jsCallInvokerHolder,
                                                jni::alias_ref<vision::VisionCameraOldScheduler::javaobject> androidScheduler);
  static void registerNatives();

  explicit FrameProcessorRuntimeManagerOld(jni::alias_ref<FrameProcessorRuntimeManagerOld::jhybridobject> jThis,
                                        jsi::Runtime* runtime,
                                        std::shared_ptr<facebook::react::CallInvoker> jsCallInvoker,
                                        std::shared_ptr<vision::VisionCameraOldScheduler> scheduler) :
      javaPart_(jni::make_global(jThis)),
      runtime_(runtime),
      jsCallInvoker_(jsCallInvoker),
      scheduler_(scheduler)
  {}

 private:
  friend HybridBase;
  jni::global_ref<FrameProcessorRuntimeManagerOld::javaobject> javaPart_;
  jsi::Runtime* runtime_;
  std::shared_ptr<facebook::react::CallInvoker> jsCallInvoker_;
  std::shared_ptr<reanimated::WorkletRuntime> workletRuntime_;
  std::shared_ptr<vision::VisionCameraOldScheduler> scheduler_;

  jni::global_ref<CameraViewOld::javaobject> findCameraViewOldById(int viewId);
  void registerPlugins();
  void initializeRuntime();
  void installJSIBindings();
  void registerPlugin(alias_ref<JFrameProcessorPlugin::javaobject> plugin);
  void logErrorToJS(const std::string& message);

  void setFrameProcessor(jsi::Runtime& runtime,                 // NOLINT(runtime/references)
                         int viewTag,
                         const jsi::Value& frameProcessor,
                         const jsi::Value& workletRuntimeValue);
  void unsetFrameProcessor(int viewTag);
};

} // namespace vision
