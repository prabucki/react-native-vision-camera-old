//
//  FrameProcessorCallback.h
//  VisionCameraOld
//
//  Created by Marc Rousavy on 11.03.21.
//  Copyright © 2021 mrousavy. All rights reserved.
//

#pragma once

#import <Foundation/Foundation.h>
#import "FrameOld.h"

typedef void (^FrameProcessorCallback) (FrameOld* frame);
