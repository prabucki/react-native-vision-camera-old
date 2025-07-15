//
//  VisionCameraOldTypes.h
//  VisionCameraOld
//
//  React Native types and constants for Swift compatibility
//

#pragma once

#import <Foundation/Foundation.h>
#import <React/RCTViewManager.h>
#import <React/RCTUIManager.h>
#import <React/RCTBridge.h>
#import <React/RCTEventDispatcher.h>
#import <React/RCTLog.h>
#import <React/RCTBridgeModule.h>
#import <React/RCTUtils.h>

#ifdef VISION_CAMERA_DISABLE_FRAME_PROCESSORS
static bool VISION_CAMERA_ENABLE_FRAME_PROCESSORS = false;
#else
static bool VISION_CAMERA_ENABLE_FRAME_PROCESSORS = true;
#endif

@interface CameraBridge: RCTViewManager

@end

@interface FrameProcessorRuntimeManagerOld : NSObject

- (instancetype)init NS_UNAVAILABLE;
- (instancetype) initWithBridge:(RCTBridge*)bridge;
- (void) installFrameProcessorBindings;

@end
