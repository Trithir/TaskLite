# TaskLite Multi-Agent Execution Plan

## Purpose
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

## Execution Flow

### Step 1: Manager reads context
- Read AGENTS.md
- Read roadmap.md
- Identify current phase

---

### Step 2: Plan work
- Decide what to implement directly
- Decide what to delegate

---

### Step 3: Spawn subagents
- Assign each subagent a single task
- Provide subagent prompt template
- Ensure tasks do not overlap

---

### Step 4: Review results
- Validate subagent output
- Check against AGENTS.md rules
- Resolve conflicts

---

### Step 5: Integrate
- Merge changes carefully
- Fix small inconsistencies
- Avoid large rewrites

---

### Step 6: Update roadmap
- Mark completed tasks
- Add notes on decisions
- Record blockers

---

### Step 7: Repeat
- Move to next phase

---

## Phase Breakdown

### Phase 0: Setup (Manager only)
- Project structure
- AGENTS.md
- roadmap.md
- base Android project

---

### Phase 1: Foundation
Subagent A:
- Room database, DAO, repository

Subagent B:
- Compose app shell, theme, entry screen

---

### Phase 2: Expanded View
Subagent A:
- Task list UI

Subagent B:
- Task behavior logic (complete, delete, uncheck)

---

### Phase 3: Interaction
Subagent A:
- Drag-and-drop reorder

Subagent B:
- Completion animation + haptics

---

### Phase 4: Widget
Subagent A:
- Glance widget implementation

Manager:
- integration and validation

---

### Phase 5: Notification
Subagent A:
- persistent notification

---

### Phase 6: Personality
Subagent A:
- empty-state prompts
- soft warnings

---

### Phase 7: QA & Polish
Manager:
- final review
- cleanup
- roadmap completion

---

## Integration Rules

- Prefer small, safe merges
- Do not rewrite large files unnecessarily
- Keep behavior aligned with AGENTS.md
- If conflicts arise, favor simplicity

---

## Failure Handling

If a subagent:
- encounters ambiguity → stop and report
- touches unrelated files → revert those changes
- introduces feature creep → remove it

Manager must:
- correct course before continuing

---

## Guiding Principle

Build TaskLite in small, clean, verifiable steps.

Do not attempt to generate the entire app at once.
