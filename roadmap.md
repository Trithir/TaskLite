# TaskLite Roadmap

## Project summary
TaskLite is a minimal Android productivity app centered on a 4x1 widget that shows the Current Task.
The goal is to reduce distraction by putting the task ahead of the app.

## Current status
- [x] Project scaffolded
- [x] Compose app shell created
- [x] Room persistence implemented
- [x] Expanded list UI implemented
- [x] Task add/edit/delete/complete flows implemented
- [x] Reordering implemented
- [x] Widget implemented
- [x] Persistent notification implemented
- [x] Haptics implemented
- [x] Empty-state personality prompts implemented
- [x] Soft overload warning implemented
- [x] Current Task external-open positioning stabilized
- [x] Completion crash regression fixed
- [x] Long-row drag offset regression fixed
- [ ] Polish and QA pass complete

---

## Phase 1: Project scaffold and architecture
### Goals
- Create Android project structure
- Set up Compose
- Set up Room
- Establish app theme and navigation
- Create core models and repository

### Tasks
- [x] Create Android project
- [x] Configure Kotlin + Compose
- [x] Add Room dependencies and database setup
- [x] Create `TaskEntity`
- [x] Create DAO methods for:
  - insert task
  - update task
  - delete task
  - get incomplete tasks ordered by sort order
  - get completed tasks ordered by completedAt desc
  - get current task
- [x] Create repository layer
- [x] Add dark theme default and light theme support

### Notes
- Keep models minimal
- Do not add fields without a product reason
- Phase 1 scaffold now exists as a single `app` module with Compose navigation, Room, and a minimal `TaskEntity` (`id`, `text`, `sortOrder`, `completedAt`).
- The scaffolded app shell successfully carried into Phase 2 without needing an architecture rewrite.
- User-side environment validation now succeeds for `java`, `adb`, and the Gradle wrapper in a VS Code bash terminal.
- Build verification is now green after the Kotlin DSL migration and dependency compatibility fixes in `app/build.gradle.kts`.

---

## Phase 2: Expanded task view
### Goals
- Build the main near-fullscreen task interface
- Support add, edit, complete, delete, and scrolling history

### Tasks
- [x] Build expanded task screen in Compose
- [x] Show recently completed tasks above Current Task
- [x] Show Current Task with subtle emphasis
- [x] Show future tasks below
- [x] Make text tap enter inline edit mode
- [x] Add task input flow at bottom
- [x] Keep expanded view open until dismissed
- [x] Truncate only where appropriate
- [x] Add delete confirmation dialog

### Notes
- Current Task should initially render as the third fully visible row
- List becomes freely scrollable after opening
- Expanded task flow is now wired to Room-backed state with add, edit, complete, uncheck, and delete-confirmation behavior.
- Initial open now scrolls near the Current Task so roughly two completed rows can sit above it when history exists, then the list remains freely scrollable.
- Reorder, haptics, and the 300ms completion pause are still pending in later phases.

---

## Phase 3: Completion and reorder behavior
### Goals
- Make task transitions feel good
- Ensure priority is driven by order

### Tasks
- [x] Implement circular completion bubble UI
- [ ] On complete:
  - [x] strike through text
  - [x] reduce emphasis
  - [x] wait ~300ms
  - [x] shift list
  - [x] promote next task to Current Task
- [x] Implement drag-and-drop reorder
- [x] Ensure reorder changes priority immediately
- [x] Implement uncheck-completed behavior as "create new task with same text at new-task location"

### Notes
- Complete now uses a stronger haptic and keeps the completed styling visible during the 300ms pause before the list shifts.
- Reorder should use light haptic
- Add currently uses a light haptic when a non-blank task is submitted.
- Reorder now works on active tasks only, using a long-press drag handle and immediate `sortOrder` persistence so dragging to the top incomplete position makes that task the Current Task.
- Platform compromise: reorder currently uses a simple swap-threshold drag interaction inside the active-task section rather than a richer animated list-reorder system. This keeps the implementation small and maintainable for v1.
- UX follow-up: improve reorder so the dragged task floats between rows with a clearer landing gap/line instead of only swapping one slot at a time.
- UX follow-up: animate completion so the list visibly shifts upward after the 300ms completion pause.

---

## Phase 4: Widget
### Goals
- Build the 4x1 home screen widget
- Surface Current Task cleanly

### Tasks
- [x] Implement widget with Jetpack Glance
- [x] Show:
  - [x] add button
  - [x] circular completion bubble
  - [x] current task text
- [x] Tap text opens expanded view
- [x] Tap bubble completes current task
- [x] Tap add opens expanded view in add mode
- [x] Ensure widget refreshes when task data changes

### Notes
- Keep widget visually simple
- No extra status clutter in v1 unless needed
- Widget now uses Glance with a receiver, local Room-backed current-task loading, and explicit open/add/complete actions.
- Widget refresh now uses a direct Glance update path plus app-side mutation hooks, after backing out the extra WorkManager hop that made updates scheduler-dependent.
- Add-mode launch state is consumed by the expanded shell so the bottom task input can be focused from the widget add action.
- Widget refresh reliability now passes device QA for reorder, edit, in-app completion, and widget completion without the earlier open/close/open workaround.

---

## Phase 5: Persistent notification
### Goals
- Optional lock-screen-adjacent access to Current Task

### Tasks
- [x] Add optional ongoing notification
- [x] Display Current Task text
- [x] Tap notification opens expanded view
- [x] Add setting/toggle for notification enablement

### Notes
- Do not add extra actions in notification for v1
- Notification now has a minimal ongoing-toggle surface in the expanded view, with local DataStore-backed enablement and an open-intent to the expanded task view.
- Notification enablement is now opt-in by default, and turning it on requests Android's notification permission when needed.
- Platform compromise: Android notification visibility still depends on the system-level notification permission/state; v1 does not add a custom runtime-permission education flow beyond the standard permission prompt.

---

## Phase 6: Personality and soft warnings
### Goals
- Add small bits of soul without adding distraction

### Tasks
- [x] Add empty-state prompt pool
- [x] Add tone categories:
  - [x] chill
  - [x] sassy
  - [x] motivational
- [x] Randomize prompt selection
- [x] Add soft warning around 50 incomplete tasks
- [x] Match warning tone to the same personality system

---

## Phase 7: Polish and QA
### Goals
- Make interactions feel solid
- Catch rough edges

### Tasks
- [x] Add haptic feedback
- [x] Verify long text truncation in widget
- [x] Verify full text visibility in expanded view
- [x] Test empty state
- [x] Test live search filtering in expanded view
- [x] Test very long lists
- [x] Test delete confirmation flow
- [x] Test reorder behavior thoroughly
- [x] Test widget refresh behavior
- [x] Test app relaunch persistence
- [x] Test notification behavior

### Notes
- Core task-transition regression coverage now exists in local unit tests for add-at-bottom, delete confirmation, reorder priority updates, uncheck-to-duplicate, and the 300ms completion pause before promotion.
- Manual/device QA is still required for widget truncation, full expanded-text visibility, widget refresh behavior, notification behavior, and relaunch persistence because those depend on Compose/Glance/system surfaces that are not fully exercised by the current unit-test layer.
- Current manual QA signal is positive: the app appears stable in desktop/emulator testing with no obvious user-visible bugs reported so far.
- Remaining device-specific validation is mainly haptics, which is intentionally deferred until the app is available on physical hardware.
- Search now lives in a simple always-visible top composer in the expanded view, with live case-insensitive filtering across active and completed tasks; reorder is intentionally disabled while a search query is active to keep filtered drag behavior boring and predictable.

---

## Decisions already locked
- App name: TaskLite
- Completion control is a circular bubble, not a square checkbox
- Expanded view stays open until dismissed
- Tap text edits inline
- New tasks are added at the bottom
- Completed tasks remain scrollable in history
- Reordering changes priority immediately
- Persistent notification opens expanded view
- Local-only, offline-only
- Dark mode default

---

## Open questions
- [x] Whether to show remaining task count in widget later
- [x] Whether to add search when lists get very long
- [x] Whether delete confirmation should be a dialog or bottom sheet

---

## Progress log
### Session notes
- Initial product concept and behavior defined
- PRD translated into implementation roadmap
- Phase 1 foundation scaffolded locally with Compose theme, navigation entry point, Room database, DAO, and repository.
- Manager decision: keep architecture work in-thread; defer delegation until there are isolated implementation slices with low file overlap.
- Early environment blocker was resolved on the user side: VS Code bash now sees `java`, `adb`, and `./gradlew`.
- Setup documentation added to `README.md` so the local Android toolchain can be installed before build verification.
- Build compatibility corrected by moving Android Gradle Plugin from `8.5.2` to `8.6.1`, because API level `35` requires AGP `8.6.0` or newer.
- README setup was corrected again after confirming the repo already includes `gradlew` and `gradlew.bat`; global Gradle install is not required.
- Wrapper configuration was corrected again after live validation: this repo currently requires Gradle `8.13`, so the wrapper was updated to match the actual build error from `./gradlew`.
- Tooling baseline moved to current stable releases for AGP, Kotlin, KSP, Compose BOM, and key AndroidX libraries while keeping `compileSdk` and `targetSdk` at `35` to avoid mixing dependency freshness with new platform behavior changes.
- Latest user-side validation results:
  - `java -version` works and resolves to Android Studio JBR `21.0.9`
  - `adb --version` works and resolves to the Android SDK platform-tools install
  - `./gradlew --version` works and downloads/uses Gradle `8.13`
  - `./gradlew tasks` now passes after migrating the Kotlin JVM target config to `compilerOptions`
  - `./gradlew assembleDebug` now passes after aligning `compileSdk` with the current AndroidX baseline and adding the XML Material theme dependency
- Build compatibility notes:
  - `app/build.gradle.kts` no longer uses deprecated `kotlinOptions { jvmTarget = "17" }`
  - `compileSdk` moved to `36` while `targetSdk` remains `35`, keeping runtime behavior unchanged while satisfying current library requirements
- Phase 2 progress this session:
  - Replaced the placeholder screen with a Room-backed expanded task view
  - Added inline edit, add-at-bottom, complete, uncheck-completed, and delete-confirmation flows
  - Kept manager-owned integration and roadmap updates in-thread while delegating isolated UI and state slices per `EXECUTION_PLAN.md`
- Phase 3 progress this session:
  - Added stronger completion haptics and light add haptics
  - Added a 300ms visual completion pause before active-list items shift
  - Kept completion styling visible during the pause so the interaction reads clearly before promotion
  - Added active-task drag reorder with immediate persistence through Room-backed `sortOrder` updates
  - Kept completed history out of the reorder path so only incomplete priority changes
  - QA feedback corrected reorder semantics so downward drags now move by the intended slot instead of overshooting
  - QA feedback corrected completed-history display so the newest completed task sits closest to the Current Task, at the bottom of the completed section
- Phase 4 progress this session:
  - Added a minimal Glance widget with local Room-backed current-task loading
  - Added widget actions for open, add, and complete
  - Added the app-side intent bridge so widget launches can distinguish add mode from a plain open
  - Investigated the widget refresh pipeline against both local code and official Android Glance guidance
  - Replaced the extra WorkManager-based refresh hop with a more direct widget update path
  - Wired widget add launches into the expanded view so the task input can be focused from the widget
  - QA feedback corrected widget sync so current-task changes from in-app reorder/update paths now trigger a durable refresh path
  - QA feedback corrected widget layout so task text no longer sits under the completion bubble
- Phase 5 progress this session:
  - Started notification groundwork with local channel/builder/open-intent plumbing
  - Added DataStore-backed notification enable/disable persistence and manager APIs for a future toggle surface
  - Wired the persisted notification-enabled state into the expanded view with a minimal toggle surface
  - Kept the notification work local and offline, without adding extra notification actions or settings sprawl
- Concurrent polish progress this session:
  - Softened the post-complete list movement with active-section size animation so the upward shift reads more intentionally
  - QA feedback corrected the expanded-screen bottom spacing so the notification and overload cards no longer cover list rows
  - QA feedback corrected completed-task interaction scope so completed history rows stay read-only except for duplicate-via-bubble
  - QA feedback corrected expanded-screen scroll behavior so initial open and post-complete motion keep the Current Task closer to the top and more consistent in view
  - QA feedback upgraded active-task dragging from tiny handle-only swaps to a broader continuous long-press drag on the active row
  - QA feedback corrected notification-open anchoring by treating each external open as a fresh launch for expanded-view positioning
  - QA feedback reduced drag drift by basing reorder movement on measured row spacing instead of a fixed threshold
  - Refactor landed: the expanded screen now renders completed tasks, active tasks, and footer cards as first-class lazy items, with active reorder keyed to row IDs instead of a grouped nested block
  - Current Task launch anchoring now uses the real flat item index instead of the older completed-count heuristic
  - Current Task launch positioning now passes device QA across widget and notification opens after the flat-list follow-up fixes
  - Investigation note: launch anchoring now waits for a brief content settle window before scrolling, and active-row drag thresholds now use neighboring row heights instead of a single dragged-row threshold
  - Regression follow-up: completion crash was caused by transient duplicate task IDs across active/completed lazy sections during the 300ms completion window; active rows now filter out pending-completion IDs before rendering
  - Regression follow-up: long-row drag drift now uses center-based anchoring so the dragged row stays closer to the pointer even when crossing rows of different heights
  - Haptics are now aligned to the product rules: strong for complete, light for add and reorder-start, and none for delete
- Phase 6 progress this session:
  - Added a small empty-state prompt pool with chill, sassy, and motivational tones
  - Randomized the prompt selection while keeping the empty-state surface minimal and calm
  - Added a soft overload warning once the active list gets large, using the same tone family instead of introducing a new alert system
- Next concrete implementation step:
  - Finish the final Phase 7 manual device QA pass against the shipped widget, notification, reorder, and text-display behaviors
  - Keep the richer floating-gap reorder UX as a later polish follow-up, not a blocker for current phase work
- Phase 7 progress this session:
  - Added a small local unit-test layer for `ExpandedTasksViewModel` so the highest-risk task transitions are covered by automation instead of only manual QA
  - Verified `testDebugUnitTest` and `assembleDebug` both pass after adding regression coverage for add, complete, uncheck, delete, and reorder behavior
  - Manager call: keep the remaining Phase 7 checklist focused on device/manual validation for widget, notification, relaunch, and visual text-behavior surfaces
- QA update:
  - Latest user testing reports the app feels stable with no obvious bugs currently noticed
  - Physical-device QA now confirms the in-app vibration patterns are working again, and widget completion vibration is also firing reliably
  - First physical-device QA pass surfaced follow-ups around keyboard/composer positioning, text auto-capitalization, drag-handle ergonomics, widget density, notification icon polish, and missing haptics
  - Follow-up implementation landed: the add-task composer now rides above the keyboard instead of shifting the full screen, task text fields request sentence capitalization, and drag now starts from the handle without a long-press
  - Follow-up implementation landed: tapping open space now clears focus so the keyboard can dismiss when the user clicks away from the add-task composer
  - Follow-up implementation landed: reorder now previews during drag but commits the move on release, so tasks do not feel like they are dropping early just by passing over another row
  - Drag-session follow-up landed: the reorder handle gesture now stays keyed to the task ID instead of the moving task order, which keeps the drag alive across row swaps
  - Follow-up implementation landed: active-task drag now gently auto-scrolls when the dragged row nears the top or bottom of the visible list
  - Widget follow-up landed: the widget now uses a single centered pill-style bar layout with aggressive `4x1` sizing hints, slimmer controls, and a lighter translucent surface; launcher-side remove/re-add may still be required on hosts that cache widget sizing hints
  - Follow-up implementation landed: notification small icon now uses a dedicated app asset instead of the generic system exclamation icon
  - Feedback follow-up landed: TaskLite now uses vibration-first feedback with distinct intensities for add, move, edit, and complete, while keeping a small `View.performHapticFeedback` fallback only when direct vibration is unavailable
  - Editor UX follow-up landed: selecting a task for inline edit now hides the add-task composer and scrolls the edited row upward so it stays visible above the keyboard on smaller screens
  - Notification follow-up landed: the active notification now uses the current task text as the title so Android no longer shows the app name twice in the shade
  - Visual refresh follow-up landed: the app and widget now share a deeper forest-green palette, lighter green task surfaces, a restrained current-task highlight, and warm complementary completion bubbles
  - Manager review follow-up: helper-agent review caught and corrected the edit-row index math for inline edit scrolling and prevented the amber accent color from leaking into unrelated controls
  - Release metadata follow-up: app package version advanced from `0.1.0` to `0.1.1`, with `versionCode` incremented to keep installs/upgrades monotonic on device
  - Feedback follow-up landed: add, move, and edit now use the former completion vibration profile, while complete now uses a stronger two-part `buzz buzzzz` confirmation
  - Widget investigation follow-up landed: two helper agents traced the visible background issue to the widget pill's full-width layout, and the widget now uses a single padded content row instead of overlapping full-width boxes
  - Widget cleanup follow-up landed: old shrink-era overlap layout was removed so text/button sizing and pill padding now live in one place
  - Drag polish follow-up landed: edge auto-scroll is softer and now respects the bottom composer height so the scroll trigger sits above the add-task bar instead of hiding underneath it
  - QA polish follow-up landed: entering inline edit now focuses the selected task field directly and opens the keyboard so editing can start immediately
  - QA polish follow-up landed: completion vibration now uses a more emphatic `buzz, pause, buzzzz` rhythm, while add, move, and edit reuse the former completion confirmation pattern
  - QA polish follow-up landed: widget current-task text was bumped one more size step, and the top edge auto-scroll trigger was nudged slightly farther from the screen edge
  - Widget feedback follow-up landed: completing the current task from the home-screen widget now triggers the same completion vibration profile as the in-app complete action
  - Cleanup follow-up landed: the shared vibration helper now keeps the completion waveform in one named place instead of duplicating that effect inline across call paths
  - Release metadata follow-up: app package version advanced from `0.1.1` to `0.1.2`, with `versionCode` incremented again so the latest build upgrades cleanly on device
  - Widget UX follow-up landed: the widget pill itself now opens the app on tap while leaving the bubble actions intact, and the complete/add bubbles now mirror the same left-right arrangement used in the expanded list
  - Feedback follow-up landed: the completion vibration timing was doubled so the `buzz, pause, buzzzz` pattern reads more clearly on-device
  - Release metadata follow-up: app package version advanced from `0.1.2` to `0.1.3`, with `versionCode` incremented again for the new widget/feedback pass
  - The old haptic-first approach is intentionally retired from the main path so the app’s tactile feedback now matches the requested vibration levels more closely
  - Widget polish follow-up landed: incomplete completion bubbles now show a checkmark inside an outlined circle in both the expanded list and the widget, while completed/pending-complete bubbles fill in during the crossed-out state
  - Keyboard follow-up landed: the expanded list now reserves both composer height and live IME inset so the bottom rows can still scroll fully into view while the keyboard is open, including the ongoing-notification card
  - Widget haptics follow-up landed: widget completion now uses a more resilient vibrator lookup path that falls back from `VibratorManager` to the legacy vibrator service when needed
  - Release metadata follow-up: app package version advanced from `0.1.3` to `0.1.4`, with `versionCode` incremented again for this widget/keyboard polish pass
  - Regression follow-up landed: direct vibration now uses the simpler shared `vibrate(effect)` path again after the newer touch-usage attribute route caused haptics to go silent across app interactions on-device
  - Widget visual follow-up landed: the idle widget completion bubble now blends into the pill background with a muted outline instead of reading as a yellow-highlighted filled control
  - Widget haptics deep-dive follow-up landed: widget completion now uses a widget-only vibration path tuned for callback/background execution, while the list keeps the foreground-tuned helper that already works well on-device
  - Widget polish follow-up landed: the widget completion state now lingers for 1 second with a filled bubble and struck-through task text before revealing the next task
  - Release metadata follow-up: app package version advanced from `0.1.4` to `0.1.5`, with `versionCode` incremented again for this widget vibration investigation pass
  - Widget double-complete follow-up landed: widget completion now serializes taps with an in-process action mutex so overlapping widget callbacks cannot race each other
  - Widget stale-click follow-up landed: widget completion callbacks now carry the rendered task ID and will only complete that exact task, so queued stale taps cannot complete the next promoted task
  - Widget cleanup follow-up landed: the temporary extra interaction-lock timing layer was removed after the task-ID validation fix made it redundant, leaving the widget completion path smaller and easier to reason about
  - Release metadata follow-up: app package version advanced from `0.1.4` to `0.1.8`, with `versionCode` incremented through the widget vibration, double-tap, stale-click, and cleanup passes
  - Current working QA signal: widget completion now vibrates, lingers for 1 second with the completed styling, and ignores stale second taps that previously completed the next task
  - Launcher icon follow-up landed: TaskLite now sets explicit adaptive launcher icons in the manifest, using a completed-bubble foreground with a calm green background plus a monochrome variant for themed icons
  - Notification icon follow-up landed: the status-bar icon now uses a minimal dot-list glyph instead of a completed checkmark so it reads more clearly as an active task list
  - Edit-scroll regression follow-up landed: inline edit anchoring now runs inside the expanded screen with the live composer height, so the selected task settles into the add-bar slot above the keyboard instead of scrolling off-screen
  - Edit-scroll follow-up landed: the inline-edit target index now matches the actual rendered list order, and the anchor leaves a bit more space so the `Delete`, `Cancel`, and `Done` row stays visible more often during edit focus
