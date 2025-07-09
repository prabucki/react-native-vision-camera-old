//
//  VisionCameraOldScheduler.h
//  VisionCameraOld
//
//  Created by Marc Rousavy on 23.07.21.
//  Copyright © 2021 mrousavy. All rights reserved.
//

#pragma once

#include <functional>

namespace vision {

class VisionCameraOldScheduler {
 public:
  void scheduleOnUI(std::function<void()> job);
};

} // namespace vision
