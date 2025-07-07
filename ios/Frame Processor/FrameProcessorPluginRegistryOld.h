//
//  FrameProcessorPluginRegistryOld.h
//  VisionCameraOld
//
//  Created by Marc Rousavy on 24.03.21.
//  Copyright © 2021 mrousavy. All rights reserved.
//

#pragma once

#import <Foundation/Foundation.h>
#import "FrameOld.h"

typedef id (^FrameProcessorPlugin) (Frame* frame, NSArray<id>* arguments);

@interface FrameProcessorPluginRegistryOld : NSObject

+ (NSMutableDictionary<NSString*, FrameProcessorPlugin>*)frameProcessorPlugins;
+ (void) addFrameProcessorPlugin:(NSString*)name callback:(FrameProcessorPlugin)callback;

@end
