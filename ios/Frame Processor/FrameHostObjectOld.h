//
//  FrameHostObjectOld.h
//  VisionCameraOld
//
//  Created by Marc Rousavy on 22.03.21.
//  Copyright © 2021 mrousavy. All rights reserved.
//

#pragma once

#import <jsi/jsi.h>
#import <CoreMedia/CMSampleBuffer.h>
#import "FrameOld.h"

using namespace facebook;

class JSI_EXPORT FrameHostObjectOld: public jsi::HostObject {
public:
  explicit FrameHostObjectOld(FrameOld* frame): frame(frame) {}

public:
  jsi::Value get(jsi::Runtime&, const jsi::PropNameID& name) override;
  std::vector<jsi::PropNameID> getPropertyNames(jsi::Runtime& rt) override;
  void close();

public:
  FrameOld* frame;

private:
  void assertIsFrameStrong(jsi::Runtime& runtime, const std::string& accessedPropName);
};
