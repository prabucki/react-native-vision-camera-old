<a href="https://margelo.io">
  <img src="./docs/static/img/banner.svg" width="100%" />
</a>

<h1 align="center">Vision Camera</h1>

<div align="center">
  <img src="docs/static/img/11.png" width="50%">
  <br />
  <br />
  <blockquote><b>📸 The Camera library that sees the vision.</b></blockquote>
  <pre align="center">npm i <a href="https://www.npmjs.com/package/react-native-vision-camera-old">react-native-vision-camera-old</a>@vc2<br/>npx pod-install                     </pre>
  <a align="center" href='https://ko-fi.com/F1F8CLXG' target='_blank'>
    <img height='36' style='border:0px;height:36px;' src='https://az743702.vo.msecnd.net/cdn/kofi2.png?v=0' border='0' alt='Buy Me a Coffee at ko-fi.com' />
  </a>
  <br/>
  <a align="center" href="https://github.com/mrousavy?tab=followers">
    <img src="https://img.shields.io/github/followers/mrousavy?label=Follow%20%40mrousavy&style=social" />
  </a>
  <br />
  <a align="center" href="https://twitter.com/mrousavy">
    <img src="https://img.shields.io/twitter/follow/mrousavy?label=Follow%20%40mrousavy&style=social" />
  </a>
</div>

<br/>
<br/>

<div>
  <img align="right" width="35%" src="docs/static/img/example.png">
</div>

### V2

This is the V2 branch of VisionCameraOld (`2.x.x` on npm, tag: `vc2`). Since VisionCameraOld V3 (current main, `3.x.x` on npm, tag: `latest`) features a full codebase rewrite on Android and a huge refactor on the iOS codebase, I will try to provide limited support for VisionCameraOld V2 for a while until V3 has been tested enough to be considered fully stable.

However, I will not provide free support for V2 anymore. I will be merging community PRs to the v2 branch if necessary as well as fixing some critical issues and releasing them, but I will not help you fix your app on v2.

It is recommended that everybody upgrades to VisionCameraOld V3. If you have any issues upgrading, ask in the community discord.

> [!NOTE]
> The documentation hosted on https://react-native-vision-camera-old.com represents the documentation for V3. For V2, read the [docs/](docs/) folder here.

VisionCameraOld V2 requires React Native 0.70+, iOS 11+, Android API 21+, Xcode 15+.


### Documentation

* [Guides](https://react-native-vision-camera-old.com/docs/guides)
* [API](https://react-native-vision-camera-old.com/docs/api)
* [Example](./example/)

### Features

* Photo, Video and Snapshot capture
* Customizable devices and multi-cameras (smoothly zoom out to "fish-eye" camera)
* Customizable FPS
* [Frame Processors](https://react-native-vision-camera-old.com/docs/guides/frame-processors) (JS worklets to run QR-Code scanning, facial recognition, AI object detection, realtime video chats, ...)
* Smooth zooming (Reanimated)
* Fast pause and resume
* HDR & Night modes

> See the [example](./example/) app

### Example

```tsx
function App() {
  const devices = useCameraDevices('wide-angle-camera')
  const device = devices.back

  if (device == null) return <LoadingView />
  return (
    <Camera
      style={StyleSheet.absoluteFill}
      device={device}
      isActive={true}
    />
  )
}
```

### Adopting at scale

<a href="https://github.com/sponsors/mrousavy">
  <img align="right" width="160" alt="This library helped you? Consider sponsoring!" src=".github/funding-octocat.svg">
</a>

VisionCameraOld is provided _as is_, I work on it in my free time.

If you're integrating VisionCameraOld in a production app, consider [funding this project](https://github.com/sponsors/mrousavy) and <a href="mailto:me@mrousavy.com?subject=Adopting VisionCameraOld at scale">contact me</a> to receive premium enterprise support, help with issues, prioritize bugfixes, request features, help at integrating VisionCameraOld and/or Frame Processors, and more.

<br />

#### 🚀 Get started by [setting up permissions](https://react-native-vision-camera-old.com/docs/guides/)!
