# TaskLite Signed-Device Smoke Test Plan

This plan reflects the current release state of TaskLite as of version `0.1.10` (`versionCode 11`).
The local signed release APK smoke pass has completed without release-blocking issues. The next validation step is Play internal-track delivery.

## What is already automated

- [x] Local unit tests cover core `ExpandedTasksViewModel` task transitions
- [x] Minimal Compose `androidTest` smoke coverage exists for:
	- empty-state launch with core controls visible
	- add-task flow from the bottom composer
- [x] Signed local `release` APK assembly succeeds
- [x] Signed local `release` AAB assembly succeeds
- [ ] Connected-device instrumentation results have been recorded
- [x] Signed physical-device smoke results have been recorded
- [ ] Play internal-track install validation has been recorded

## Recommended local signed-release workflow

Use this path for repeatable physical-device smoke testing before Play upload.

1. Confirm release signing is configured locally.
	- Command: `.\\gradlew.bat :app:printReleaseSigningStatus`
	- Input source: `release-signing.properties` or all `TASKLITE_RELEASE_*` environment variables
2. Build the signed local release APK.
	- Command: `.\\gradlew.bat :app:assembleRelease`
	- Artifact: `app\\build\\outputs\\apk\\release\\app-release.apk`
3. Install the signed release APK on a physical device.
	- Command: `adb install -r app\\build\\outputs\\apk\\release\\app-release.apk`
4. Run the manual smoke checklist below against that installed build.

Why this is the recommended local path:
- It uses the real `release` build type
- It exercises release signing, manifest merge, minification, and resource shrinking
- It is much faster for repeated smoke passes than generating split APKs from the AAB

## Current status of that local path

- [x] Signed release APK built successfully
- [x] Installed on a physical device
- [x] Physical-device smoke pass completed without release-blocking issues

## Final pre-upload validation path

Use Play internal testing as the last release validation step.

1. Build the signed bundle.
	- Command: `.\\gradlew.bat :app:bundleRelease`
	- Artifact: `app\\build\\outputs\\bundle\\release\\app-release.aab`
2. Upload that AAB to an internal Play track.
3. Install from Play on a physical device.
4. Re-run the highest-risk behavior checks:
	- widget placement and refresh
	- notification visibility/privacy behavior
	- persistence after relaunch/upgrade
	- launcher icon and notification icon rendering

Manager call:
- Local signed APK install is the practical smoke path and is now validated.
- Play internal-track install is the remaining release-delivery validation path.
- `bundletool` is optional, not the default workflow.

## Manual signed-device smoke checklist

The following local release checks have now been exercised without release-blocking issues:

- [x] First launch and empty state
- [x] Core add, edit, complete, uncheck, and delete flows
- [x] Reorder behavior
- [x] Widget behavior
- [x] Notification behavior and privacy surface spot-check
- [x] Persistence after relaunch
- [x] Physical-device haptics
- [x] Empty-state and long-list behavior

## Small automated smoke scope

The current `androidTest` layer is intentionally narrow.

Automated today:
- first screen renders with core controls
- empty-state surface renders
- add-task composer can submit a task and clear itself

Not automated today:
- widget behavior
- notification shade behavior
- launcher-specific rendering
- physical-device vibration quality
- release-install behavior on a real device
- persistence across actual process death, reboot, or upgrade install

## Build verification vs behavior verification

Build verification already covered:
- signed `assembleRelease`
- signed `bundleRelease`
- release minification/resource shrinking
- unit tests
- minimal Compose smoke test compilation

Behavior verification already covered locally:
- signed release installed and exercised on a real physical device
- widget behavior spot-checked on a launcher
- notification permission/privacy behavior spot-checked
- vibration feel spot-checked
- relaunch persistence spot-checked

Behavior verification still pending:
- Play-delivered internal-track install behavior
- Play pre-launch report signal
- upgrade/install flow from Play delivery

## Current blockers that remain manual

- [ ] Play internal-track install verified
- [ ] Play pre-launch report reviewed
