declare module 'react-native-vision-camera-old' {
  import React from 'react';

  // Core types
  export interface FrameOld {
    isValid: boolean;
    width: number;
    height: number;
    bytesPerRow: number;
    planesCount: number;
    toString(): string;
    close(): void;
  }

  export type CameraPosition = 'front' | 'back' | 'external';
  export type CameraPermissionStatus = 'authorized' | 'not-determined' | 'denied' | 'restricted';
  export type CameraPermissionRequestResult = 'authorized' | 'denied';

  export interface CameraDevice {
    id: string;
    position: CameraPosition;
    name: string;
    hasFlash: boolean;
    hasTorch: boolean;
    isMultiCam: boolean;
    minZoom: number;
    maxZoom: number;
    neutralZoom: number;
    formats: any[];
    supportsParallelVideoProcessing: boolean;
    supportsLowLightBoost: boolean;
    supportsDepthCapture: boolean;
    supportsRawCapture: boolean;
    supportsFocus: boolean;
  }

  export interface CameraProps {
    device: CameraDevice;
    isActive: boolean;
    frameProcessor?: (frame: FrameOld) => void;
    frameProcessorFps?: number | 'auto';
    style?: any;
    zoom?: number;
    onInitialized?: () => void;
    onError?: (error: any) => void;
    onFrameProcessorPerformanceSuggestionAvailable?: (suggestion: any) => void;
  }

  // Camera component
  export class Camera extends React.PureComponent<CameraProps> {
    static requestCameraPermission(): Promise<CameraPermissionRequestResult>;
    static getCameraPermissionStatus(): Promise<CameraPermissionStatus>;
    static getAvailableCameraDevices(): Promise<CameraDevice[]>;
  }

  // Hooks
  export function useFrameProcessor(
    frameProcessor: (frame: FrameOld) => void,
    dependencies: React.DependencyList
  ): (frame: FrameOld) => void;

  export function useCameraDevices(): {
    back?: CameraDevice;
    front?: CameraDevice;
    external?: CameraDevice;
  };

  export function useCameraFormat(
    device: CameraDevice | undefined,
    filters: any[]
  ): any;

  // Additional types that might be needed
  export interface Point {
    x: number;
    y: number;
  }

  export interface PhotoFile {
    path: string;
    width: number;
    height: number;
    orientation: any;
    isMirrored: boolean;
    thumbnail?: any;
  }

  export interface VideoFile {
    path: string;
    duration: number;
  }

  export class CameraRuntimeError extends Error {
    code: string;
    message: string;
    cause?: Error;
    constructor(code: string, message: string, cause?: Error);
  }
}
