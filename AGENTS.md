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
  - `orchestrator`: request classification, role selection, sequencing, result integration
  - `idea-director`: idea discovery, differentiation, opportunity finding
  - `product-planner`: goals, user flow, state, event, exception planning
  - `ux-designer`: usability, flow, cognitive load, action design
  - `visual-designer`: visual polish, hierarchy, spacing, emotional finish
  - `ui-system-designer`: reusable patterns, component consistency, structural polish
  - `user-advocate`: raw consumer perspective, emotional friction, delight gaps
  - `pm`: scope control, prioritization, sequencing
  - `technical-architect`: structure, state ownership, long-term stability
  - `developer`: implementation planning anchored to existing project structure
  - `mobile-developer`: Android/Compose implementation, state handling, smallest safe change
  - `qa-reviewer`: regression risk, missing validation, QA review
  - `release-inspector`: pre-release UX and edge-case checks
  - `copywriter`: product microcopy and tone polish
  - `data-observer`: retention and repeated-use perspective
  - `documentation-owner`: action-list documentation and rollout summary
  - `kotlin-specialist`: use when Kotlin/state structure analysis is specifically needed
- If possible, spawn real subagents for role work.
- Current local role availability under `.codex/agents`:
  - available: `orchestrator`, `idea-director`, `product-planner`, `ux-designer`, `visual-designer`, `ui-system-designer`, `user-advocate`, `pm`, `technical-architect`, `developer`, `mobile-developer`, `qa-reviewer`, `release-inspector`, `copywriter`, `data-observer`, `documentation-owner`, `kotlin-specialist`
- Fallback rule:
  - when runtime subagent limits prevent spawning every desired role, prefer `orchestrator` first and then only the smallest necessary subset of specialist roles

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
  - write a commit message in Korean by default unless the user asks otherwise
  - commit the changes
  - push the branch
  - after push completes, merge it into `main`
  - delete the finished working branch after merge to avoid branch clutter
- If a git write operation such as `commit`, `merge`, or branch deletion is blocked by sandbox permissions, immediately retry with the available escalation path instead of asking the user to do it manually.
- If push, PR, or merge cannot be completed because of environment, auth, remote, or policy constraints, clearly explain the reason in the final report.
