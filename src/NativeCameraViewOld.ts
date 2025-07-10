import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

export interface Spec extends TurboModule {
  // Camera permission methods
  getCameraPermissionStatus(): Promise<string>;
  getMicrophonePermissionStatus(): Promise<string>;
  requestCameraPermission(): Promise<string>;
  requestMicrophonePermission(): Promise<string>;

  // Camera device methods
  getAvailableCameraDevices(): Promise<object[]>;

  // Camera operations
  takePhoto(viewTag: number, options: object): Promise<object>;
  takeSnapshot(viewTag: number, options: object): Promise<object>;
  startRecording(viewTag: number, options: object): Promise<void>;
  stopRecording(viewTag: number): Promise<void>;
  pauseRecording(viewTag: number): Promise<void>;
  resumeRecording(viewTag: number): Promise<void>;
  focus(viewTag: number, point: object): Promise<void>;
}

export default TurboModuleRegistry.getEnforcing<Spec>('CameraViewOld');
