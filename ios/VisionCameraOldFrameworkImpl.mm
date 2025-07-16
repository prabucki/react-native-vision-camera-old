//
//  VisionCameraOldFrameworkImpl.mm
//  VisionCameraOld
//
//  Framework implementation to bridge interfaces declared in VisionCameraOldTypes.h
//

#import <Foundation/Foundation.h>
#import <React/RCTBridge.h>

// Import the actual implementation (this will only be used at link time, not compile time for the module)
#import "Frame Processor/FrameProcessorRuntimeManagerOld.h"

// We don't need to redeclare the interface here since it's already in VisionCameraOldTypes.h
// The linker will connect our interface declaration to this implementation

// This file ensures that the implementation methods exist when Swift calls them
// The actual FrameProcessorRuntimeManagerOld class provides all the implementation
