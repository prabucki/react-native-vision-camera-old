//
//  FrameOld.m
//  VisionCameraOld
//
//  Created by Marc Rousavy on 08.06.21.
//  Copyright © 2021 mrousavy. All rights reserved.
//

#import "FrameOld.h"
#import <Foundation/Foundation.h>
#import <CoreMedia/CMSampleBuffer.h>

@implementation FrameOld {
  CMSampleBufferRef buffer;
  UIImageOrientation orientation;
}

- (instancetype) initWithBuffer:(CMSampleBufferRef)buffer orientation:(UIImageOrientation)orientation {
  self = [super init];
  if (self) {
    _buffer = buffer;
    _orientation = orientation;
  }
  return self;
}

@synthesize buffer = _buffer;
@synthesize orientation = _orientation;

@end
