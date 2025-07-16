//
//  VisionCameraOldWrapper.mm
//  VisionCameraOld
//
//  Implementation of wrapper functions for Swift compatibility
//

#import "VisionCameraOldWrapper.h"
#import "Frame Processor/FrameProcessorRuntimeManagerOld.h"

// C-style wrapper implementations
FrameProcessorRuntimeManagerPtr CreateFrameProcessorRuntimeManager(RCTBridge* bridge) {
    FrameProcessorRuntimeManagerOld* manager = [[FrameProcessorRuntimeManagerOld alloc] initWithBridge:bridge];
    return (__bridge_retained void*)manager;
}

void InstallFrameProcessorBindings(FrameProcessorRuntimeManagerPtr manager) {
    FrameProcessorRuntimeManagerOld* objcManager = (__bridge FrameProcessorRuntimeManagerOld*)manager;
    [objcManager installFrameProcessorBindings];
}

void ReleaseFrameProcessorRuntimeManager(FrameProcessorRuntimeManagerPtr manager) {
    FrameProcessorRuntimeManagerOld* objcManager = (__bridge_transfer FrameProcessorRuntimeManagerOld*)manager;
    objcManager = nil; // Release the object
}
