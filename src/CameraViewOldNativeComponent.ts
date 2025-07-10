import type { ViewProps } from 'react-native';
import type { HostComponent } from 'react-native';
import codegenNativeComponent from 'react-native/Libraries/Utilities/codegenNativeComponent';
import type {
  DirectEventHandler,
  Double,
  Int32,
  WithDefault,
} from 'react-native/Libraries/Types/CodegenTypes';

export interface NativeProps extends ViewProps {
  // Camera configuration
  cameraId?: string;
  isActive?: WithDefault<boolean, false>;

  // Use cases
  photo?: WithDefault<boolean, false>;
  video?: WithDefault<boolean, false>;
  audio?: WithDefault<boolean, false>;

  // Frame processor
  enableFrameProcessor?: WithDefault<boolean, false>;
  frameProcessorFps?: WithDefault<Double, 30>;

  // Format configuration
  format?: object;
  fps?: WithDefault<Int32, 30>;
  hdr?: WithDefault<boolean, false>;
  lowLightBoost?: WithDefault<boolean, false>;
  colorSpace?: WithDefault<string, 'srgb'>;
  videoStabilizationMode?: WithDefault<string, 'off'>;

  // Other properties
  preset?: WithDefault<string, 'medium'>;
  torch?: WithDefault<string, 'off'>;
  zoom?: WithDefault<Double, 1.0>;
  enableDepthData?: WithDefault<boolean, false>;
  enableHighQualityPhotos?: WithDefault<boolean, false>;
  enablePortraitEffectsMatteDelivery?: WithDefault<boolean, false>;
  enableZoomGesture?: WithDefault<boolean, false>;
  orientation?: WithDefault<string, 'portrait'>;

  // Events
  onViewReady?: DirectEventHandler<{}>;
  onInitialized?: DirectEventHandler<{}>;
  onError?: DirectEventHandler<{
    code: string;
    message: string;
    cause?: object;
  }>;
  onFrameProcessorPerformanceSuggestionAvailable?: DirectEventHandler<{
    type: string;
    suggestedFrameProcessorFps: Double;
  }>;
}

export default codegenNativeComponent<NativeProps>('CameraViewOld') as HostComponent<NativeProps>;
