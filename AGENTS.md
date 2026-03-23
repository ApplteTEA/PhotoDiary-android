# AGENTS

## Project Mission
- This app is a Photo Diary based Android diary app.
- The current phase is v1.5 polishing, stabilization, and emotional finish work rather than large feature expansion.
- Core screens are `Main`, `Write`, `Detail`, `Calendar`, `MyPage`, and `Monthly Reflection`.

## Product Priorities
- Priority order is:
  1. Monthly Reflection stabilization
  2. Monthly Reflection visual finish
  3. Main archive tone polish
  4. Write screen tone polish
  5. Theme experience polish
- Login, backup, sync, and large feature expansion are lower priority.
- The project is in v1.5 polishing/stabilization mode, not feature-first expansion mode.

## Non-Negotiable Constraints
- Do not do large structural refactors.
- Do not replace navigation wholesale.
- Preserve edge-to-edge behavior.
- Do not add dark mode.
- Keep standard Gradle configuration.
- Do not introduce changes that harm Android Studio sync or build stability.

## UX Direction
- `Write` should feel like a record space, not a cold system form.
- `Detail` should feel like a reading screen, not an edit form.
- `Monthly Reflection` is the key differentiating feature of the app.
- `Main` should feel more like an archive than a feed.
- Always prefer small changes that improve polish and completeness.

## Team Workflow
- For non-trivial work, operate as a role-based team:
  - `planner`: requirements, scope control, prioritization, sequencing
  - `product-designer`: UX structure, hierarchy, emotional tone, microcopy, polish
  - `mobile-developer`: Android/Compose implementation, state handling, smallest safe change
  - `reviewer`: regression risk, missing validation, QA review
  - `kotlin-specialist`: use only when Kotlin/state structure analysis is specifically needed
- If possible, spawn real subagents for role work.
- Current local role availability under `.codex/agents`:
  - available: `mobile-developer`, `reviewer`, `kotlin-specialist`
  - not present as dedicated local agent files: `planner`, `product-designer`
- Fallback rule:
  - use `default` subagents for `planner` and `product-designer` when dedicated local files are unavailable
  - use the local `.codex/agents` roles directly for `mobile-developer`, `reviewer`, and `kotlin-specialist`

## Required Operating Pattern Before Non-Trivial Changes
- First present:
  - current state summary
  - core problem definition
  - minimal change plan
  - regression risks
  - manual QA checklist

## Required Reporting Pattern After Code Changes
- Always summarize:
  - changed files
  - reason for each change
  - regression risks
  - manual QA checklist
  - remaining issues

## Finalization Protocol For Completed Work
- Only when the user's full requested scope is complete, finalize work in this order:
  1. self-check of the implemented changes
  2. final review through the `reviewer` role when possible, or the closest available review procedure
  3. summarize regression risks, missing tests, and manual QA checklist
  4. create a GitHub branch when possible
  5. create a PR when possible
  6. if PR creation is not possible, clearly explain why and replace it with a local final review result
- Do not create a branch or PR for partial progress or intermediate milestones.

## Branch And Delivery Policy
- Start each new non-trivial task on a new branch.
- After the full requested scope is complete:
  - write a commit message
  - commit the changes
  - push the branch
  - after push completes, merge it into `main`
- If push, PR, or merge cannot be completed because of environment, auth, remote, or policy constraints, clearly explain the reason in the final report.
