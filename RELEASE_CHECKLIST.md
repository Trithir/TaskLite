# TaskLite Release Checklist

This checklist reflects the current repository state as of version `0.1.10` (`versionCode 11`).
Signed release builds work, signed-release physical-device smoke has passed, and the remaining work is now mostly internal-track validation plus store/policy/admin setup.

## Technical readiness

- [x] Release signing is wired through `release-signing.properties` or `TASKLITE_RELEASE_*` environment variables.
- [x] Signed local release APK builds successfully with `.\\gradlew.bat :app:assembleRelease`.
- [x] Signed local release AAB builds successfully with `.\\gradlew.bat :app:bundleRelease`.
- [x] Release shrinking and resource shrinking are enabled.
- [x] Signed release APK physical-device smoke passed with no release-blocking issues found.
- [x] A minimal Compose `androidTest` smoke layer exists for launch/add coverage.
- [x] The current release path is documented in [TEST_PLAN.md](/c:/Users/Erics/DEV/TaskLite/TEST_PLAN.md).
- [ ] Upload the signed `AAB` to Play internal testing.
- [ ] Install from Play internal testing and verify install/upgrade behavior.
- [ ] Re-check widget, notification, persistence, and icon rendering from the Play-installed build.
- [ ] Review Play pre-launch report results once available.
- [ ] Follow the manual upload and submission notes in [PLAY_SUBMISSION_NOTES.md](/c:/Users/Erics/DEV/TaskLite/PLAY_SUBMISSION_NOTES.md).

## Technical notes that may need explanation

- [x] App manifest is lean: `POST_NOTIFICATIONS` and `VIBRATE`.
- [x] Release merged manifest also includes transitive AndroidX/WorkManager surface from Glance:
	- `WAKE_LOCK`
	- `ACCESS_NETWORK_STATE`
	- `RECEIVE_BOOT_COMPLETED`
	- `FOREGROUND_SERVICE`
- [x] Current manager call remains: accept that transitive surface for now because removing it blindly is higher risk than shipping it, and the app still does not declare `INTERNET`.
- [ ] Be ready to explain in Play review notes, if needed, that the extra background permissions/components come from widget support libraries and are not used to transmit task data off-device.

## Store listing and assets

- [ ] Finalize [STORE_LISTING_DRAFT.md](/c:/Users/Erics/DEV/TaskLite/STORE_LISTING_DRAFT.md) into the exact store copy to paste into Play Console.
- [ ] Capture final phone screenshots from the real release build.
- [ ] Ensure the screenshot set includes:
	- widget on home screen
	- expanded list with current task context
	- inline edit state
	- reorder or long-list/search state
	- empty state
	- optional notification
- [ ] Decide whether to prepare a feature graphic.
- [ ] If a feature graphic will be used, prepare final copy/art direction and export.
- [ ] Confirm launcher icon, monochrome icon, notification icon, and widget visuals are final enough for store assets.
- [ ] Confirm whether a separate Play listing icon export is needed from the existing launcher artwork.

## Privacy, policy, and Play Console forms

- [ ] Host a public privacy policy URL based on [PRIVACY.md](/c:/Users/Erics/DEV/TaskLite/PRIVACY.md).
- [ ] Enter Data safety answers that match the actual app:
	- no user data collected off-device in the current implementation
	- no user data shared with third parties
	- task data stored locally on-device
	- notification permission used only for the optional ongoing notification
	- vibration permission used for tactile feedback
- [ ] Complete App content declarations:
	- privacy policy
	- ads declaration
	- app access if required
	- content rating
	- target audience
	- news status if applicable
- [ ] Confirm developer account contact requirements are satisfied:
	- support email
	- phone/address/account verification items required by Play for the account type

## Manual Play Console metadata still needed

- [ ] Default language
- [ ] App category
- [ ] App name
- [ ] Short description
- [ ] Full description
- [ ] Support email
- [ ] Support website if available
- [ ] Privacy policy URL
- [ ] Tester list or tester access path for internal testing
- [ ] Release notes for the internal test build

## Versioning and release hygiene

- [x] App identity rename is complete in-repo: `com.erics.tasklite` -> `io.tasklite`.
- [ ] Treat first tester installs of `io.tasklite` as fresh installs, not upgrade validation from `com.erics.tasklite`.
- [ ] Decide the public-facing `versionName` for the first Play release.
- [ ] Increment `versionCode` before the internal-track upload if a newer artifact than the already-tested signed APK/AAB will be generated.
- [x] Current strategy is sane: monotonic `versionCode`, simple human-readable `versionName`, and a repo-supported signed AAB path.
- [ ] Decide whether Play App Signing will be used and store signing material accordingly.

## Remaining blockers, split by type

Technical blockers:
- [ ] Play internal-track upload and Play-installed validation are still pending.

Asset blockers:
- [ ] Final screenshots are still missing.
- [ ] Feature graphic decision/export is still pending.

Policy/admin blockers:
- [ ] Hosted privacy policy URL is still missing.
- [ ] Play Console Data safety and app content forms are still incomplete.
- [ ] Final support/contact metadata still needs to be entered in Play Console.
