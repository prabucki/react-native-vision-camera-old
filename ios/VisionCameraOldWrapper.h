//
//  VisionCameraOldWrapper.h
//  VisionCameraOld
//
//  Wrapper functions for Swift compatibility to avoid direct class conflicts
//

#pragma once

#import <Foundation/Foundation.h>
#import <React/RCTBridge.h>

// Opaque pointer to avoid exposing the actual class interface
typedef void* FrameProcessorRuntimeManagerPtr;

// C-style wrapper functions that Swift can call
FrameProcessorRuntimeManagerPtr CreateFrameProcessorRuntimeManager(RCTBridge* bridge);
void InstallFrameProcessorBindings(FrameProcessorRuntimeManagerPtr manager);
void ReleaseFrameProcessorRuntimeManager(FrameProcessorRuntimeManagerPtr manager);
