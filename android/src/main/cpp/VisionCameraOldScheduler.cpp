//
// Created by Marc Rousavy on 25.07.21.
//

#include "VisionCameraOldScheduler.h"
#include <fbjni/fbjni.h>
#include <fbjni/NativeRunnable.h>
#include <utility>

namespace vision {

using namespace facebook;

using TSelf = jni::local_ref<VisionCameraOldScheduler::jhybriddata>;

TSelf VisionCameraOldScheduler::initHybrid(jni::alias_ref<jhybridobject> jThis) {
  return makeCxxInstance(jThis);
}

void VisionCameraOldScheduler::scheduleOnUI(std::function<void()> job) {
  static const auto method = javaPart_->getClass()->getMethod<void(jni::JRunnable::javaobject)>("scheduleOnUI");
  auto jrunnable = jni::JNativeRunnable::newObjectCxxArgs(std::move(job));
  method(javaPart_.get(), jrunnable.get());
}

void VisionCameraOldScheduler::registerNatives() {
  registerHybrid({
    makeNativeMethod("initHybrid", VisionCameraOldScheduler::initHybrid),
  });
}

} // namespace vision
