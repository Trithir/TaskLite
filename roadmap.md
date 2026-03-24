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
- [ ] Verify long text truncation in widget
- [ ] Verify full text visibility in expanded view
- [ ] Test empty state
- [ ] Test very long lists
- [ ] Test delete confirmation flow
- [ ] Test reorder behavior thoroughly
- [ ] Test widget refresh behavior
- [ ] Test app relaunch persistence
- [ ] Test notification behavior

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
- [ ] Whether to show remaining task count in widget later
- [ ] Whether to add search when lists get very long
- [ ] Whether delete confirmation should be a dialog or bottom sheet

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
  - Finish the final Phase 7 QA pass against the shipped widget, notification, reorder, and text-display behaviors
  - Keep the richer floating-gap reorder UX as a later polish follow-up, not a blocker for current phase work
