require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

reactVersion = '0.0.0'
begin
  reactVersion = JSON.parse(File.read(File.join(__dir__, "..", "react-native", "package.json")))["version"]
rescue
  reactVersion = '0.66.0'
end
rnVersion = reactVersion.split('.')[1]

folly_flags = '-DFOLLY_NO_CONFIG -DFOLLY_MOBILE=1 -DFOLLY_USE_LIBCPP=1 -DRNVERSION=' + rnVersion
folly_compiler_flags = folly_flags + ' ' + '-Wno-comma -Wno-shorten-64-to-32'
folly_version = '2021.04.26.00'
boost_compiler_flags = '-Wno-documentation'

Pod::Spec.new do |s|
  s.name         = "VisionCameraOld"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.homepage     = package["homepage"]
  s.license      = package["license"]
  s.authors      = package["author"]

  s.platforms    = { :ios => "11.0" }
  s.source       = { :git => "https://github.com/mrousavy/react-native-vision-camera-old.git", :tag => "#{s.version}" }

  # Configure for static framework
  s.static_framework = true

  s.pod_target_xcconfig = {
    "USE_HEADERMAP" => "YES",
    "HEADER_SEARCH_PATHS" => "\"$(PODS_TARGET_SRCROOT)/ReactCommon\" \"$(PODS_TARGET_SRCROOT)\" \"$(PODS_ROOT)/RCT-Folly\" \"$(PODS_ROOT)/boost\" \"$(PODS_ROOT)/boost-for-react-native\" \"$(PODS_ROOT)/DoubleConversion\" \"$(PODS_ROOT)/Headers/Private/React-Core\" \"$(PODS_ROOT)/../../node_modules/react-native-reanimated/Common/cpp\" ",
    "GCC_PREPROCESSOR_DEFINITIONS[config=Release]" => "$(inherited) NDEBUG=1",
    "DEFINES_MODULE" => "YES"
  }
  s.compiler_flags = folly_compiler_flags + ' ' + boost_compiler_flags
  s.xcconfig = {
    "CLANG_CXX_LANGUAGE_STANDARD" => "c++17",
    "HEADER_SEARCH_PATHS" => "\"$(PODS_ROOT)/boost\" \"$(PODS_ROOT)/boost-for-react-native\" \"$(PODS_ROOT)/glog\" \"$(PODS_ROOT)/RCT-Folly\" \"${PODS_ROOT}/Headers/Public/React-hermes\" \"${PODS_ROOT}/Headers/Public/hermes-engine\"",
    "OTHER_CFLAGS" => "$(inherited)" + " " + folly_flags,
    "DEFINES_MODULE" => "YES"
  }

  s.requires_arc = true

  # All source files including Swift
  s.source_files = [
    "ios/**/*.{m,mm,h,swift}",
    "cpp/**/*.{cpp,h}"
  ]

  # Only Objective-C headers that don't have C++ dependencies should be public
  s.public_header_files = [
    "ios/VisionCameraOld.h",
    "ios/CameraBridge.h",
    "ios/Frame Processor/FrameOld.h",
    "ios/Frame Processor/FrameProcessorCallback.h",
    "ios/Frame Processor/FrameProcessorRuntimeManagerOld.h",
    "ios/Frame Processor/FrameProcessorPluginRegistryOld.h",
    "ios/Frame Processor/FrameProcessorPlugin.h",
    "ios/React Utils/RCTBridge+runOnJS.h",
    "ios/React Utils/JSConsoleHelper.h"
  ]

  # Make all C++ headers and problematic headers private
  s.private_header_files = [
    "ios/Frame Processor/VisionCameraOldScheduler.h",
    "ios/Frame Processor/FrameHostObjectOld.h",
    "ios/React Utils/JSIUtils.h",
    "cpp/**/*.h"
  ]

  s.dependency "React-callinvoker"
  s.dependency "React"
  s.dependency "React-Core"
  s.dependency "RNReanimated"
end
