//
//  FrameHostObjectOld.h
//  VisionCameraOld
//
//  Created by Marc Rousavy on 22.03.21.
//  Copyright © 2021 mrousavy. All rights reserved.
//

#pragma once

#import <jsi/jsi.h>
#import <memory>
#import <vector>

#import "FrameOld.h"

using namespace facebook;

class FrameHostObjectOld: public jsi::HostObject, public std::enable_shared_from_this<FrameHostObjectOld> {
public:
  explicit FrameHostObjectOld(FrameOld* frame);
  ~FrameHostObjectOld();

public:
  jsi::Value get(jsi::Runtime&, const jsi::PropNameID& name) override;
  std::vector<jsi::PropNameID> getPropertyNames(jsi::Runtime& rt) override;

public:
  void close();

private:
  void assertIsFrameStrong(jsi::Runtime& runtime, const std::string& accessedPropName);
  static void cleanupOldFrames();

private:
  FrameOld* frame;
  static std::vector<std::shared_ptr<FrameHostObjectOld>> frames;
};
