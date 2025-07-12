//
// Created by Marc Rousavy on 22.03.21.
//

#include "FrameHostObjectOld.h"
#include <jni.h>
#include <fbjni/fbjni.h>
#include <android/log.h>

#include <vector>
#include <memory>
#include <algorithm>

#include "ImageProxyUtils.h"

namespace vision {

using namespace facebook;

// Static frame tracking for memory management
std::vector<std::weak_ptr<FrameHostObjectOld>> FrameHostObjectOld::frames;

FrameHostObjectOld::FrameHostObjectOld(const jni::alias_ref<JImageProxy::javaobject>& imageProxy)
    : imageProxy(jni::make_global(imageProxy)) {
  // Add to global frames list for memory management
  frames.push_back(shared_from_this());

  // Clean up old frames if we have too many
  if (frames.size() > 10) {
    cleanupOldFrames();
  }
}

FrameHostObjectOld::~FrameHostObjectOld() {
  // Cleanup is handled by cleanupOldFrames()
}

void FrameHostObjectOld::cleanupOldFrames() {
  // Remove expired weak pointers
  frames.erase(std::remove_if(frames.begin(), frames.end(),
    [](const std::weak_ptr<FrameHostObjectOld>& ptr) {
      return ptr.expired();
    }), frames.end());

  // If still too many, remove oldest ones
  if (frames.size() > 5) {
    frames.erase(frames.begin(), frames.begin() + (frames.size() - 5));
  }
}

std::vector<jsi::PropNameID> FrameHostObjectOld::getPropertyNames(jsi::Runtime& rt) {
  std::vector<jsi::PropNameID> result;
  result.push_back(jsi::PropNameID::forUtf8(rt, std::string("toString")));
  result.push_back(jsi::PropNameID::forUtf8(rt, std::string("isValid")));
  result.push_back(jsi::PropNameID::forUtf8(rt, std::string("width")));
  result.push_back(jsi::PropNameID::forUtf8(rt, std::string("height")));
  result.push_back(jsi::PropNameID::forUtf8(rt, std::string("bytesPerRow")));
  result.push_back(jsi::PropNameID::forUtf8(rt, std::string("planesCount")));
  result.push_back(jsi::PropNameID::forUtf8(rt, std::string("close")));
  return result;
}

jsi::Value FrameHostObjectOld::get(jsi::Runtime& runtime, const jsi::PropNameID& propName) {
  auto name = propName.utf8(runtime);

  if (name == "toString") {
    return jsi::Function::createFromHostFunction(runtime, jsi::PropNameID::forAscii(runtime, "toString"), 0, [=](jsi::Runtime& runtime, const jsi::Value& thisValue, const jsi::Value* arguments, size_t count) -> jsi::Value {
      if (!this->imageProxy) {
        return jsi::String::createFromUtf8(runtime, "[closed frame]");
      }

      auto width = this->imageProxy->getWidth();
      auto height = this->imageProxy->getHeight();
      auto string = std::to_string(width) + " x " + std::to_string(height) + " Frame";
      return jsi::String::createFromUtf8(runtime, string);
    });
  }

  if (name == "isValid") {
    if (this->imageProxy == nullptr) {
      return jsi::Value(false);
    }

    static const auto isImageProxyValid = jni::findClassStatic("com/mrousavy/old/camera/frameprocessor/ImageProxyUtils")
        ->getStaticMethod<jboolean(jni::alias_ref<JImageProxy::javaobject>)>("isImageProxyValid");

    bool isValid = isImageProxyValid(jni::findClassStatic("com/mrousavy/old/camera/frameprocessor/ImageProxyUtils"), this->imageProxy);
    return jsi::Value(isValid);
  }

  if (name == "width") {
    this->assertIsFrameStrong(runtime, name);
    return jsi::Value(this->imageProxy->getWidth());
  }

  if (name == "height") {
    this->assertIsFrameStrong(runtime, name);
    return jsi::Value(this->imageProxy->getHeight());
  }

  if (name == "bytesPerRow") {
    this->assertIsFrameStrong(runtime, name);

    static const auto getBytesPerRow = jni::findClassStatic("com/mrousavy/old/camera/frameprocessor/ImageProxyUtils")
        ->getStaticMethod<jint(jni::alias_ref<JImageProxy::javaobject>)>("getBytesPerRow");

    int bytesPerRow = getBytesPerRow(jni::findClassStatic("com/mrousavy/old/camera/frameprocessor/ImageProxyUtils"), this->imageProxy);
    return jsi::Value(bytesPerRow);
  }

  if (name == "planesCount") {
    this->assertIsFrameStrong(runtime, name);

    static const auto getPlanesCount = jni::findClassStatic("com/mrousavy/old/camera/frameprocessor/ImageProxyUtils")
        ->getStaticMethod<jint(jni::alias_ref<JImageProxy::javaobject>)>("getPlanesCount");

    int planesCount = getPlanesCount(jni::findClassStatic("com/mrousavy/old/camera/frameprocessor/ImageProxyUtils"), this->imageProxy);
    return jsi::Value(planesCount);
  }

  if (name == "close") {
    return jsi::Function::createFromHostFunction(runtime, jsi::PropNameID::forAscii(runtime, "close"), 0, [=](jsi::Runtime& runtime, const jsi::Value& thisValue, const jsi::Value* arguments, size_t count) -> jsi::Value {
      this->close();
      return jsi::Value::undefined();
    });
  }

  return jsi::Value::undefined();
}

void FrameHostObjectOld::assertIsFrameStrong(jsi::Runtime& runtime, const std::string& accessedPropName) {
  if (this->imageProxy == nullptr) {
    auto message = "Cannot get `" + accessedPropName + "` from a Frame that has already been closed! Did you call `frame.close()`?";
    throw jsi::JSError(runtime, message);
  }
}

void FrameHostObjectOld::close() {
  if (this->imageProxy != nullptr) {
    this->imageProxy->close();
    this->imageProxy = nullptr;
  }
}

} // namespace vision
