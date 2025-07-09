//
//  FrameProcessorPluginRegistryOld.mm
//  VisionCameraOld
//
//  Created by Marc Rousavy on 24.03.21.
//  Copyright © 2021 mrousavy. All rights reserved.
//

#import "FrameProcessorPluginRegistryOld.h"
#import <Foundation/Foundation.h>

@implementation FrameProcessorPluginRegistryOld

+ (NSMutableDictionary<NSString*, FrameProcessorPlugin>*)frameProcessorPlugins {
  static NSMutableDictionary<NSString*, FrameProcessorPlugin>* plugins = nil;
  if (plugins == nil) {
    plugins = [[NSMutableDictionary alloc] init];
  }
  return plugins;
}

+ (void) addFrameProcessorPlugin:(NSString*)name callback:(FrameProcessorPlugin)callback {
  BOOL alreadyExists = [[FrameProcessorPluginRegistryOld frameProcessorPlugins] valueForKey:name] != nil;
  NSAssert(!alreadyExists, @"Tried to two Frame Processor Plugins with the same name! Either choose unique names, or remove the unused plugin.");

  [[FrameProcessorPluginRegistryOld frameProcessorPlugins] setValue:callback forKey:name];
}

@end
