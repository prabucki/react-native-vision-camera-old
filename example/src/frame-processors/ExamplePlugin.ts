/* global __example_plugin __example_plugin_swift */
import type { FrameOld } from 'react-native-vision-camera-old';

declare let _WORKLET: true | undefined;

export function examplePluginSwift(frame: FrameOld): string[] {
  'worklet';
  if (!_WORKLET) throw new Error('examplePluginSwift must be called from a frame processor!');

  // @ts-expect-error because this function is dynamically injected by VisionCameraOld
  return __example_plugin_swift(frame, 'hello!', 'parameter2', true, 42, { test: 0, second: 'test' }, ['another test', 5]);
}

export function examplePlugin(frame: FrameOld): string[] {
  'worklet';
  if (!_WORKLET) throw new Error('examplePlugin must be called from a frame processor!');

  // @ts-expect-error because this function is dynamically injected by VisionCameraOld
  return __example_plugin(frame, 'hello!', 'parameter2', true, 42, { test: 0, second: 'test' }, ['another test', 5]);
}
