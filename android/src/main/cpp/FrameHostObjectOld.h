//
// Created by Marc Rousavy on 22.03.21.
//

#pragma once

#include <jsi/jsi.h>
#include <jni.h>
#include <fbjni/fbjni.h>
#include <memory>
#include <vector>

#include "java-bindings/JImageProxy.h"

namespace vision {

using namespace facebook;

class FrameHostObjectOld : public jsi::HostObject, public std::enable_shared_from_this<FrameHostObjectOld> {
public:
  explicit FrameHostObjectOld(const jni::alias_ref<JImageProxy::javaobject>& imageProxy);
  ~FrameHostObjectOld();

public:
  jsi::Value get(jsi::Runtime& runtime, const jsi::PropNameID& propName) override;
  std::vector<jsi::PropNameID> getPropertyNames(jsi::Runtime& rt) override;

public:
  void close();

private:
  void assertIsFrameStrong(jsi::Runtime& runtime, const std::string& accessedPropName);
  static void cleanupOldFrames();

private:
  jni::global_ref<JImageProxy::javaobject> imageProxy;
  static std::vector<std::weak_ptr<FrameHostObjectOld>> frames;
};

} // namespace vision
