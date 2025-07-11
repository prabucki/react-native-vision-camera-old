//
//  FrameProcessorPlugin.h
//  VisionCameraOld
//
//  Created by Marc Rousavy on 01.05.21.
//  Copyright © 2021 mrousavy. All rights reserved.
//

#ifndef FrameProcessorPlugin_h
#define FrameProcessorPlugin_h

#import <Foundation/Foundation.h>
#import "FrameProcessorPluginRegistryOld.h"
#import "FrameOld.h"

@protocol FrameProcessorPluginBase
+ (id) callback:(FrameOld*)frame withArgs:(NSArray<id>*)args;
@end


#define VISION_CONCAT2(A, B) A##B
#define VISION_CONCAT(A, B) VISION_CONCAT2(A, B)

/**
 * Use this Macro to register the given function as a Frame Processor.
 * * Make sure the given function is a C-style function with the following signature: static inline id callback(FrameOld* frame, NSArray* args)
 * * Make sure the given function's name is unique across other frame processor plugins
 * * Make sure your frame processor returns a Value that can be converted to JS
 * * Make sure to use this Macro in an @implementation, not @interface
 *
 * The JS function will have the same name as the given Objective-C function, but with a "__" prefix.
 * Make sure to add that function to the babel.config.js under reanimated's "globals" option, and add TypeScript type declarations.
 */
#define VISION_EXPORT_FRAME_PROCESSOR(frame_processor)                              \
                                                                                    \
+(void)load                                                                   \
{                                                                                   \
  [FrameProcessorPluginRegistryOld addFrameProcessorPlugin:@"__" @ #frame_processor callback:^id(FrameOld* frame, NSArray<id>* args) { \
    return frame_processor(frame, args);                                            \
  }];                                                                               \
}


/**
 * Same as VISION_EXPORT_FRAME_PROCESSOR, but uses __attribute__((constructor)) for
 * registration. Useful for registering swift classes that forbids use of +(void)load.
 */
#define VISION_EXPORT_SWIFT_FRAME_PROCESSOR(name, objc_name) \
objc_name : NSObject<FrameProcessorPluginBase>                                      \
@end                                                                                \
                                                                                    \
@interface objc_name (FrameProcessorPlugin) <FrameProcessorPluginBase>              \
@end                                                                                \
@implementation objc_name (FrameProcessorPlugin)                                    \
                                                                                    \
__attribute__((constructor)) static void VISION_CONCAT(initialize_, objc_name)()    \
{                                                                                   \
  [FrameProcessorPluginRegistryOld addFrameProcessorPlugin:@"__" @ #name callback:^id(FrameOld* frame, NSArray<id>* args) { \
    return [objc_name callback:frame withArgs:args];                              \
  }];                                                                             \
}

#endif /* FrameProcessorPlugin_h */
