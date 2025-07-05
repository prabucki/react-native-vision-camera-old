//
// Created by Marc Rousavy on 25.07.21.
//

#pragma once

#include <jni.h>
#include <fbjni/fbjni.h>

namespace vision {

using namespace facebook;

class VisionCameraOldScheduler : public jni::HybridClass<VisionCameraOldScheduler> {
 public:
  static auto constexpr kJavaDescriptor = "Lcom/mrousavy/old/camera/frameprocessor/VisionCameraOldScheduler;";
  static jni::local_ref<jhybriddata> initHybrid(jni::alias_ref<jhybridobject> jThis);
  static void registerNatives();

  // schedules the given job to be run on the VisionCameraOld FP Thread at some future point in time
  void scheduleOnUI(std::function<void()> job);

 private:
  friend HybridBase;
  jni::global_ref<VisionCameraOldScheduler::javaobject> javaPart_;

  explicit VisionCameraOldScheduler(jni::alias_ref<VisionCameraOldScheduler::jhybridobject> jThis):
    javaPart_(jni::make_global(jThis)) {}
};

} // namespace vision
