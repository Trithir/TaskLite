# AGENTS.md

## Project
TaskLite is a minimal, offline-first Android productivity app centered around a 4x1 home screen widget that displays the user's Current Task.

The app is intentionally narrow in scope:
- widget-first
- one current task
- frictionless task access
- minimal distraction
- no feature creep

## Product principles
- The task is the product, not the app.
- Keep users out of the phone as much as possible.
- Prefer the shortest path to viewing, adding, reordering, editing, and completing tasks.
- The app should feel calm, intentional, and lightweight.
- Slight personality is allowed in text prompts only.
- Do not add organizational systems that increase cognitive load.

## Explicit anti-features
Do not add:
- tags
- projects
- deadlines
- reminders
- categories
- sharing
- syncing
- accounts
- smart sorting
- gamification
- streaks
- sub-tasks unless explicitly requested later

## Platform and stack
- Android app
- Kotlin
- Jetpack Compose for app UI
- Jetpack Glance for widget
- Room for local persistence
- local-only storage
- fully offline
- dark mode default, light mode optional

## Core behavior
- The widget is 4x1 and shows:
  - add button
  - current task text
  - circular completion bubble
- The completion control must be a circular bubble, not a square checkbox.
- Tapping task text opens the expanded list view.
- Tapping the circular bubble completes the current task.
- Tapping add opens expanded view and focuses a new task input.
- Expanded view stays open until dismissed by the user.
- Tapping task text in expanded view edits inline.
- New tasks are inserted at the top of the active task list and become the Current Task.
- Reordering changes priority immediately.
- Dragging a task to the top incomplete position makes it the Current Task.
- Completed tasks remain in history and are scrollable.
- Unchecking a completed task creates a new active task with the same text at the top of the active list, while leaving the completed task unchanged.
- Deleting a task means it is no longer needed, not completed.
- Deletion requires confirmation.

## UI behavior
- Current Task should initially appear as the third fully visible row in expanded view.
- The first two-ish visible rows above it should be recently completed tasks.
- The list is scrollable and not anchored after opening.
- Completed tasks use strikethrough and reduced emphasis.
- Current Task gets subtle emphasis only.
- Long task text should truncate in the widget with ellipsis.
- Full text is visible in expanded view.
- Completion should have a 300ms visual pause before the list shifts.
- Haptics:
  - complete = strong/longer
  - add/reorder = light/shorter
  - delete = none

## Personality
- Slightly playful text is okay only in:
  - empty-state messages
  - soft warning messages
- Tone pools:
  - chill
  - sassy
  - motivational
- Keep the UI itself calm and uncluttered.

## Constraints
- Prefer simple, maintainable implementations.
- Prefer boring architecture over clever architecture.
- Avoid premature abstraction.
- Keep code readable.
- Do not rewrite unrelated files.
- When making progress, update roadmap.md:
  - mark completed steps
  - add brief notes
- note blockers and follow-ups
- Use single tab indentation (not spaces).
- Extract complex logic into well-named helper functions.
- Do not duplicate logic across multiple files.

### Error handling
- Handle expected errors gracefully.
- Do not over-engineer error handling.

### Comments
- Do not add obvious comments.
- Add comments only when:
  - explaining non-obvious behavior
  - documenting important decisions
- Prefer readable code over commented code.

## Definition of done for each phase
A phase is only done when:
- code builds
- core interactions for that phase work
- roadmap.md is updated
- obvious TODOs are recorded

## QA handoff
- At the end of each sprint or phase with user-visible progress, provide a short manual QA checklist tailored to that sprint's shipped behavior.
- The checklist should focus on what the user can verify right now in the app or build, not future features.
- Keep the checklist concise and practical.
- Include blockers, known gaps, and anything intentionally not testable yet.



## Purpose of roles
Defines how the manager agent and subagents collaborate to build TaskLite.

This file is about execution strategy, not product behavior.

---

## Roles

### Manager Agent
The manager agent is responsible for:
- reading AGENTS.md and roadmap.md
- deciding current phase
- making architecture decisions
- spawning subagents for isolated tasks
- reviewing and integrating subagent work
- updating roadmap.md
- preventing feature creep

The manager agent owns:
- architecture
- integration
- roadmap updates

---

### Subagents
Subagents are responsible for:
- completing a single, clearly defined task
- modifying only relevant files
- following AGENTS.md rules
- reporting:
  - summary of changes
  - files touched
  - blockers
  - follow-up notes

Subagents do NOT:
- make architecture decisions
- update roadmap.md (unless explicitly told)
- refactor unrelated code
- add new features

---

## Delegation Rules

Only delegate tasks that are:
- isolated
- low-overlap
- clearly defined
- testable independently

Do NOT delegate:
- architecture decisions
- cross-cutting refactors
- vague or open-ended tasks

---

## Concurrency

- Use 2 to 4 subagents maximum at a time
- Each subagent must work on separate concerns
- Avoid overlapping file edits

---
