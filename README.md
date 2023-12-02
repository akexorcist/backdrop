![Backdrop app screenshot](./image/image_001.jpg)
## Backdrop
Video and audio projection app for your streaming content on macOS

![Backdrop app screenshot](./image/image_002.jpg)

## Features
* Select and display video input properties supported
* Change video resolution supported
* Select audio input/output supported
* Best for sharing your screen with friends on Live Streaming Apps like Discord

![Sharing Backdrop screen on Discord](./image/image_003.png)

## Interface
![screenshot_003.jpg](./image/image_004.jpg)
1. Close app
2. Enter or exit fullscreen
3. Hide UI
4. Video input picker
5. Audio input picker
6. Audio output picker

## Build an app from source code
Prerequisite
* Xcode
* JDK 17
* Terminal or alternative CLI Tools

```
git clone https://github.com/akexorcist/backdrop.git
cd backdrop
./gradlew run
```

## Build and install an app on your macOS device with self-signed certificates
* Build an app to `.dmg` file with `./gradlew packageDmg`
  * Output file will be in `<project>/build/compose/binaries/main/dmg`
* Open `.dmg` file and move the app to `Applications` directory
* Create self-signed certificate from `Keychain Access`
  * `Keychain access` > `Certificate Assistant` > `Create a Certificate...`
  * Identity Type: `Self Signed Root`
  * Certificate Type: `Code Signing`
* Do manual codesign in command line with your self-signed certificate
  * `codesign -fs <certificate_name> --deep <path_to_app>`
  * `codesign -fs Akexorcist --deep /Applications/Backdrop.app`
* Finally, you can run the app on your machine with your self-signed certificate

## Powered by
* [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
* [Material 3](https://m3.material.io/)
* [Webcam Capture API](https://github.com/sarxos/webcam-capture)
* [Native Driver for Webcam Capture API](https://github.com/eduramiba/webcam-capture-driver-native)

## Troubleshooting
### The video list does not refresh when a device is plugged in
There's an issue with the Webcam Capture API. For a workaround, close the app and reopen it

## License
[Apache License, Version 2.0](./LICENSE.txt)
