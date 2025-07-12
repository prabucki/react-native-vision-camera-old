//
//  FrameHostObjectOld.mm
//  VisionCameraOld
//
//  Created by Marc Rousavy on 22.03.21.
//  Copyright © 2021 mrousavy. All rights reserved.
//

#import "FrameHostObjectOld.h"
#import <React/RCTBridge+Private.h>
#import <React/RCTUtils.h>
#import <ReactCommon/RCTTurboModule.h>
#import <React/RCTLog.h>

#import "FrameOld.h"
#import "JSConsoleHelper.h"

std::vector<std::shared_ptr<FrameHostObjectOld>> FrameHostObjectOld::frames;

FrameHostObjectOld::FrameHostObjectOld(FrameOld* frame): frame(frame) {
  // Add to global frames list for memory management
  frames.push_back(shared_from_this());

  // Clean up old frames if we have too many
  if (frames.size() > 10) {
    cleanupOldFrames();
  }
}

FrameHostObjectOld::~FrameHostObjectOld() {
  // Remove from global frames list
  frames.erase(std::remove_if(frames.begin(), frames.end(),
    [this](const std::weak_ptr<FrameHostObjectOld>& ptr) {
      return ptr.expired() || ptr.lock().get() == this;
    }), frames.end());
}

void FrameHostObjectOld::cleanupOldFrames() {
  // Remove expired weak pointers and limit to 5 most recent frames
  frames.erase(std::remove_if(frames.begin(), frames.end(),
    [](const std::shared_ptr<FrameHostObjectOld>& ptr) {
      return !ptr || ptr->frame == nil;
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
      if (!this->frame) {
        return jsi::String::createFromUtf8(runtime, "[closed frame]");
      }
      auto width = this->frame.width;
      auto height = this->frame.height;

      auto string = std::to_string(width) + " x " + std::to_string(height) + " Frame";
      return jsi::String::createFromUtf8(runtime, string);
    });
  }
  if (name == "isValid") {
    return jsi::Value(this->frame && this->frame.buffer != nil);
  }
  if (name == "width") {
    this->assertIsFrameStrong(runtime, name);
    return jsi::Value((double) this->frame.width);
  }
  if (name == "height") {
    this->assertIsFrameStrong(runtime, name);
    return jsi::Value((double) this->frame.height);
  }
  if (name == "bytesPerRow") {
    this->assertIsFrameStrong(runtime, name);
    return jsi::Value((double) this->frame.bytesPerRow);
  }
  if (name == "planesCount") {
    this->assertIsFrameStrong(runtime, name);
    return jsi::Value((double) this->frame.planesCount);
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
  if (!this->frame) {
    auto message = "Cannot get `" + accessedPropName + "` from a Frame that has already been closed! Did you call `frame.close()`?";
    throw jsi::JSError(runtime, message);
  }
}

void FrameHostObjectOld::close() {
  if (this->frame) {
    this->frame = nil;
  }
}
