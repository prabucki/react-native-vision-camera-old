#include <jni.h>
#include <fbjni/fbjni.h>
#include "FrameProcessorRuntimeManagerOld.h"
#include "CameraViewOld.h"
#include "VisionCameraOldScheduler.h"

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *) {
  return facebook::jni::initialize(vm, [] {
    vision::FrameProcessorRuntimeManagerOld::registerNatives();
    vision::CameraViewOld::registerNatives();
    vision::VisionCameraOldScheduler::registerNatives();
  });
}
