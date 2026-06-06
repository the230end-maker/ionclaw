# Android App

IonClaw includes a native **Kotlin / Jetpack Compose** app for Android. It embeds the full C++ engine through a small JNI bridge packaged in the `ionclaw.aar` and runs the server locally on the device — no companion app or remote backend required.

## Requirements

- Android SDK (compileSdk 36)
- Android NDK — set `ANDROID_NDK_ROOT` or pass `ANDROID_NDK=...` (to build the native runtime)
- JDK 17+

## Project Structure

`apps/android/` is only a container holding **two fully independent Gradle builds** — the same split as `apps/flutter/` (`plugin/` + `runner/`):

```
apps/android/
  library/   standalone aar project: jni bridge + kotlin api + native runtime
  app/       standalone app project: compose ui (server control + webview panel)
```

Neither is a Gradle module of the other. The `library` builds a distributable `.aar`; the `app` consumes the prebuilt `.aar`. Each has its own wrapper, `settings.gradle.kts`, and `gradle/libs.versions.toml`.

### library — the aar (bundle-id `com.ionclaw.lib`)

```
library/
  settings.gradle.kts        standalone build (rootProject "ionclaw")
  build.gradle.kts           com.android.library + NDK/CMake
  gradle/libs.versions.toml  own version catalog
  src/main/cpp/              jni bridge (ionclaw_jni.cpp) + CMakeLists.txt
  src/main/java/com/ionclaw/lib/        kotlin api
  src/main/jniLibs/<abi>/libionclaw.so  prebuilt native runtime (copied by make)
  src/main/jniLibs/<abi>/libomp.so      openmp runtime the engine links against (copied by make)
  src/main/res/drawable*/ic_notification  notification icon
```

The aar ships **64-bit ABIs only** (`arm64-v8a` + `x86_64`). 32-bit arm is dropped because llama.cpp does not build for it (it uses NEON FP16 intrinsics absent on `armeabi-v7a`), and 32-bit is legacy. The engine links OpenMP, so the NDK's `libomp.so` is bundled per ABI alongside `libionclaw.so` and loaded first by `IonClawNative` — otherwise `dlopen` of the runtime fails at launch.

The Kotlin API wraps the public C ABI (`main/lib/include/ionclaw/ionclaw.h`):

- `IonClawNative` — the `external` declarations; loads `libionclaw.so` then `libionclaw_jni.so`.
- `IonClawRuntime` — typed wrapper (`initializeProject` / `startServer` / `stopServer`) that parses the JSON returned by the native calls.
- `IonClawPlatform` — handles the `invoke_platform` callback (local notifications).

Kotlin cannot call C directly the way Swift can, so the bridge `ionclaw_jni.cpp` exposes `Java_com_ionclaw_lib_*` functions that forward to the C ABI and links against the prebuilt `libionclaw.so`. This is why the aar needs a Gradle library project to build it — it is not a raw binary like the Apple XCFramework.

### app — the application (bundle-id `com.ionclaw.app`)

```
app/
  settings.gradle.kts        standalone single-project build (rootProject "IonClaw")
  build.gradle.kts           com.android.application + compose; consumes the aar
  gradle/libs.versions.toml  own version catalog
  src/main/java/com/ionclaw/app/   compose ui, config, server viewmodel, network
```

The app references the prebuilt aar exactly like the Apple app references the prebuilt XCFramework: a `flatDir` repository in `settings.gradle.kts` pointing at `build/android-aar`, plus `implementation(":ionclaw@aar")` in `build.gradle.kts`. The app never builds the library, so the `.aar` must be assembled first.

## Build and Run

1. Build the aar (standalone, distributable):

```bash
make build-android-aar
```

This builds `libionclaw.so` for the 64-bit ABIs if not already present (`make link-android`, which also bundles `libomp.so`), then assembles `apps/android/library` into `build/android-aar/ionclaw.aar`.

2. Build, install, and launch the app on a connected device or emulator:

```bash
make run-android
```

This ensures the aar exists, runs `./gradlew installDebug` in `apps/android/app`, and starts the activity via `adb`.

Both projects need a configured Android SDK — either `local.properties` (`sdk.dir=...`) inside `apps/android/app` and `apps/android/library`, or the `ANDROID_HOME` environment variable.

## Screens

| Screen | Highlights |
|---|---|
| **Server** | Start/stop the server, edit host/port, view the run status and the LAN addresses (tap to copy), and open the web panel. |
| **Panel** | The local web panel in an embedded `WebView`, pointed at `http://localhost:<port>/app/`, with microphone permission granted for voice features. |

## Local notifications

The agent can show a local notification through the `invoke_platform` tool: `invoke_platform("local-notification.send", {"title": "...", "message": "..."})`. The handler lives in `IonClawPlatform`, registered once at launch via `ionclaw_set_platform_handler` and responding asynchronously via `ionclaw_platform_respond` (the same C ABI the Apple and Flutter apps use). It posts the notification with `NotificationCompat` using the bundled `ic_notification` icon.

`POST_NOTIFICATIONS` (Android 13+) is requested at launch and the channel is created on first registration. To add more platform functions, extend the `when` in `IonClawPlatform.dispatch(...)`.

## The aar (distribution)

`make build-android-aar` produces a self-contained `ionclaw.aar` — the native runtime (`libionclaw.so` + `libomp.so`) for the 64-bit ABIs, the JNI bridge, and the Kotlin API — independent of the app. Anyone can build and distribute it without touching the app, mirroring how `make build-xcframework` produces a standalone XCFramework.

| Platform | Artifact | Build (standalone) | App consumes |
|---|---|---|---|
| **Android** | `build/android-aar/ionclaw.aar` | `make build-android-aar` | `flatDir` + `implementation(":ionclaw@aar")` |
| **Apple** | `build/xcframework/ionclaw.xcframework` | `make build-xcframework` | referenced in `project.yml` |

## Notes

- App icons (adaptive `ic_launcher`) and the `logo` live in `app/src/main/res`. The notification icon (`ic_notification`) lives in the library so it ships inside the aar with the notification code.
- The native `libionclaw.so` is the same binary used by the Flutter Android plugin — see [Flutter](flutter.md).
- The panel uses `localhost` (the host the server reports for a `0.0.0.0` bind), not the literal `127.0.0.1`, which some device network stacks treat differently.
- The panel `WebView` is loaded only after it has been laid out (`doOnLayout`), so the page resolves a non-zero viewport height; loading before the view has a size collapses the app shell to zero height and renders blank.

## Clean

```bash
make clean-android   # remove Android native builds, jniLibs, and the aar
```
