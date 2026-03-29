# TaskLite Play Submission Notes

This note turns the remaining Google Play work into concrete manual steps.
It is intentionally scoped to the app that exists today.

## Internal-track upload path

1. Confirm release signing is configured locally.
2. Build the signed bundle:
	- `.\\gradlew.bat :app:bundleRelease`
3. Upload:
	- `app\\build\\outputs\\bundle\\release\\app-release.aab`
4. Create or use an internal testing track in Play Console.
5. Add tester emails or tester groups.
6. Install the Play-delivered build on a physical device.
7. Re-run the high-risk checks from [TEST_PLAN.md](/c:/Users/Erics/DEV/TaskLite/TEST_PLAN.md).

Notes:
- Play internal testing is the correct final validation path for the signed AAB delivery flow.
- Internal testing is limited to a small tester set, so decide the tester list before upload.
- If a new artifact is generated after this point, increment `versionCode` before uploading it.

## Versioning hygiene

- Current repo version: `versionName "0.1.10"` and `versionCode 11`
- Current app ID: `io.tasklite`
- Previous local/dev app ID: `com.erics.tasklite`
- This strategy is sane for an initial Play release:
	- `versionCode` is monotonic
	- `versionName` is human-readable
- Because the app ID changed before store release, treat Play/internal installs as fresh installs rather than upgrade validation from the old package.
- Before the first Play upload:
	- choose the final initial release `versionName`
	- increment `versionCode` once for the actual uploaded build if any last-minute build is produced after current validation

## Manual Play Console metadata still needed

- App title: `TaskLite`
- Default language
- Short description
- Full description
- App category: Productivity
- Contact email
- Support website if available
- Privacy policy URL
- App icon asset if Play requires updated upload/export packaging
- Phone screenshots
- Optional feature graphic
- Release notes for the internal build
- Target audience / content rating answers
- App content forms and declarations

## Suggested Data safety answers to verify in Play Console

Based on the current app implementation:

- Personal info collected: No
- Financial info collected: No
- Messages collected: No
- Photos/videos collected: No
- Audio files collected: No
- Files/docs collected: No
- Calendar collected: No
- Contacts collected: No
- App activity collected: No
- Web browsing collected: No
- Device or other IDs collected: No

Task data exists only as local on-device storage in the current app.
There is no account system, no network API, no ads SDK, no analytics SDK, and no crash-reporting SDK in the current codebase.
The app does not declare `INTERNET`.

## Permissions / manifest items that may need explanation

### `POST_NOTIFICATIONS`

- Purpose: optional ongoing current-task notification
- Important qualifier: not required for core app or widget use
- Android-version dependent: only relevant where Android requires notification permission

### `VIBRATE`

- Purpose: tactile feedback for add, edit, reorder, complete, and widget completion interactions

### Widget receiver / Glance surface

- The app includes an app widget receiver for the 4x1 home screen widget
- The current release manifest also picks up some WorkManager-related components transitively through Glance
- Current manager decision: acceptable for this release because there is still no `INTERNET` permission and the surface comes from the widget stack, not a network feature
- If Play review asks about background-related components or permissions, explain that they come from AndroidX Glance/WorkManager widget support rather than a sync or analytics feature

## Store asset checklist

### Required screenshots

- Widget on home screen with current task visible
- Main expanded view showing current task, completed history, and future tasks
- Inline edit state
- Long-list/search state
- Empty state

### Optional screenshots

- Ongoing notification enabled
- Completed-history example if it reads clearly

### Feature graphic

- Keep it simple and calm
- Use the real TaskLite color system
- Avoid mock dashboards or fake sync/productivity claims
- If text is included, keep it short and plain

### Icon / branding deliverables still needed

- Final Play listing icon export review
- Confirmation that launcher icon, round icon, monochrome icon, and notification icon are final enough for release

## Remaining blocker categories

### Technical

- Play internal-track install has not yet been verified
- Play pre-launch report has not yet been reviewed

### Asset

- Final screenshots are not prepared yet
- Feature graphic is not prepared yet

### Policy / admin

- Privacy policy must be hosted at a public URL
- Contact/support details must be supplied in Play Console
- Developer account verification items must be satisfied for the account type
- Data safety and app content forms still need manual completion
- Content rating and target audience forms still need manual completion
