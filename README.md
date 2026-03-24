# TaskLite

TaskLite is a minimal, offline-first Android productivity app centered on a 4x1 widget that shows your current task.

## Setup

This project is currently Windows-first because that is the active development environment.
The shell examples below assume a bash terminal in VS Code.

You need:
- Android Studio
- JDK 17
- Android SDK Platform 36
- Android SDK Build-Tools 36.x
- Android SDK Platform-Tools
- Android Emulator and at least one Android 15+ system image if you want emulator testing

Notes:
- Android Studio already includes a JDK and the emulator, so the fastest path is to install Android Studio first and let its setup wizard install the SDK pieces.
- This repo currently targets `compileSdk = 36`, `targetSdk = 35`, `minSdk = 31`, and uses Android Gradle Plugin `8.13.2`.
- The Gradle wrapper committed in this repo should be used for terminal builds.

## Recommended Install Path

### 1. Install Android Studio

1. Download Android Studio from the official Android Developers site.
2. Run the Windows installer.
3. Keep the default options unless you have a reason to change them.
4. Let the Android Studio Setup Wizard install the recommended SDK components.

Official docs:
- https://developer.android.com/studio/install
- https://developer.android.com/studio/

### 2. Confirm JDK 17

Android Studio bundles a compatible JDK, which is enough for most local development.

If you want a standalone JDK on your machine for terminal use:
1. Install JDK 17.
2. Set `JAVA_HOME` to that JDK path.
3. Add `$JAVA_HOME/bin` to your shell `PATH`.
4. Reopen your bash terminal and run `java -version`.

Official compatibility docs:
- https://developer.android.com/build/releases/agp-8-6-0-release-notes
- https://developer.android.com/build/releases/about-agp

### 3. Install the Android SDK Packages This Project Needs

In Android Studio:
1. Open `Tools > SDK Manager`.
2. In `SDK Platforms`, install `Android SDK Platform 36`.
3. In `SDK Tools`, install:
- `Android SDK Build-Tools` and pick the latest `36.x.x`
- `Android SDK Platform-Tools`
- `Android Emulator`
- `Android SDK Command-line Tools (latest)`
4. Apply the changes and accept licenses.

Official docs:
- https://developer.android.com/about/versions/15/setup-sdk
- https://developer.android.com/tools/sdkmanager
- https://developer.android.com/tools

### 4. Optional: Create an Emulator

1. Open `Tools > Device Manager`.
2. Create a phone emulator.
3. Download a recent Android system image if prompted.
4. Start the emulator once to confirm it boots.

Official docs:
- https://developer.android.com/studio/run/emulator

### 5. Optional: Enable Command-Line Android Tools (Git Bash)

1. To use adb and sdkmanager in Git Bash:

```
code ~/.bashrc 
```
and add:

```
export ANDROID_SDK_ROOT="/c/Users/Erics/AppData/Local/Android/Sdk"
export ANDROID_HOME="$ANDROID_SDK_ROOT"
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
export PATH="$JAVA_HOME/bin:$ANDROID_SDK_ROOT/platform-tools:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$PATH"
```

Reload your shell:

```
source ~/.bashrc
```

Verify:
```
adb --version
```


Official docs:
- https://developer.android.com/tools
- https://developer.android.com/tools/sdkmanager

### 6. Use the Gradle Wrapper

This repo already includes `gradlew` and `gradlew.bat`, so you do not need to install Gradle globally just to build the project.

1. Open a bash terminal in the repo root.
2. Make sure `java -version` works first.
3. Run `./gradlew --version`.
4. Run `./gradlew tasks`.

The wrapper downloads the project’s required Gradle version automatically.

Official compatibility docs:
- https://developer.android.com/build/releases/agp-8-6-0-release-notes

## First Run

1. Open this folder in Android Studio.
2. Let Gradle sync finish.
3. If Android Studio asks for missing SDK components, install them.
4. Select an emulator or connected Android device.
5. Run the `app` configuration.
6. If you want to build from bash, run `./gradlew tasks` once from the repo root to confirm the wrapper setup is working.

## Quick Verification

From bash, these should work after setup:

```bash
java -version
adb --version
./gradlew --version
./gradlew tasks
```

## Bash Note

If your VS Code bash terminal is Git Bash, the Windows Android SDK and Android Studio installs usually work fine with the path style shown above.

If your VS Code bash terminal is WSL, the cleanest path is usually one of these:
- use Android Studio on Windows for SDK management and app runs
- or install the full JDK and Android SDK toolchain inside WSL separately

The current README examples are written for Git Bash on Windows.

## Current Known Limitation

Build verification is now working, but widget refresh behavior is still under active device QA because it depends on launcher/widget-host timing as well as app logic.
