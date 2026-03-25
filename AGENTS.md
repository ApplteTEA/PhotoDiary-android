# AGENTS

## Project Mission
- This app is a Photo Diary based Android diary app.
- The current phase prioritizes v1.5 polishing, stabilization, and emotional finish work, while still allowing carefully scoped feature expansion when it meaningfully improves the product.
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
  - `orchestrator`: request classification, agent selection, sequencing, conflict resolution, integration
  - `researcher`: external references, patterns, competitor examples, current best practices, uncertainty notes
  - `product-planner`: goals, scope, user flow, states, events, edge cases, acceptance criteria
  - `product-designer`: UX, UI hierarchy, spacing, alignment, state completeness, copy direction
  - `technical-architect`: state ownership, event flow, file impact, safest implementation plan
  - `android-implementer`: Android/Compose implementation with minimal invasive change
  - `qa-gate`: functional QA, UX QA, regression review, final pass/fail gate
- If possible, spawn real subagents for role work.
- Current local role availability under `.codex/agents`:
  - available: `orchestrator`, `researcher`, `product-planner`, `product-designer`, `technical-architect`, `android-implementer`, `qa-gate`
- Fallback rule:
  - when runtime subagent limits prevent spawning every desired role, prefer `orchestrator` first and then only the smallest necessary subset of roles required to complete the current stage
- Default pipeline:
  1. `orchestrator`
  2. `researcher` when external patterns or current references matter
  3. `product-planner`
  4. `product-designer`
  5. `technical-architect` when structure or state decisions are needed
  6. `android-implementer`
  7. `qa-gate`
- Mandatory role rules:
  - implementers do not invent product requirements
  - implementers do not make independent design decisions when design intent is available
  - QA is a separate final judge, not just implementer self-check
  - `orchestrator` must not mark work complete without a QA pass/fail decision
  - ambiguous requests must be sent back to `product-planner` before implementation

## Required Operating Pattern Before Non-Trivial Changes
- First present:
  - current state summary
  - core problem definition
  - minimal change plan
  - regression risks
  - manual QA checklist

## Mandatory UI Review Rules
- For UI/UX-facing changes, do not stop at implementation.
- Always run a strict self-check for:
  - awkward spacing jumps
  - inconsistent information hierarchy
  - mismatched component grammar at the same level
  - overgrown labels or helper text
  - box-inside-box layering without a strong reason
  - elements that feel too large for their importance
- For meaningful UI changes, prefer this review order:
  - `product-planner`
  - `product-designer`
  - `technical-architect` when structure is affected
  - `android-implementer`
  - `qa-gate`
- If `qa-gate` would likely reject the result, do not present it as finished.
- Treat “looks weird”, “feels off”, “spacing is strange”, and similar feedback as correctness issues, not just taste issues.

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
  2. final review through the `qa-gate` role when possible, or the closest available review procedure
  3. for UI/UX-facing changes, do not report completion without a `qa-gate` style pass/fail check
  4. summarize regression risks, missing tests, and manual QA checklist
  5. create a GitHub branch when possible
  6. create a PR when possible
  7. if PR creation is not possible, clearly explain why and replace it with a local final review result
- Do not create a branch or PR for partial progress or intermediate milestones.

## Branch And Delivery Policy
- By default, work directly on `main` unless the user explicitly asks for a separate branch or PR workflow.
- After the full requested scope is complete:
  - write a commit message in Korean by default unless the user asks otherwise
  - commit the changes
  - push the current branch
  - if working on a separate branch by explicit request, merge it into `main` after push
  - if a temporary working branch was used, delete it after merge to avoid branch clutter
- If a git write operation such as `commit`, `merge`, or branch deletion is blocked by sandbox permissions, immediately retry with the available escalation path instead of asking the user to do it manually.
- If push, PR, or merge cannot be completed because of environment, auth, remote, or policy constraints, clearly explain the reason in the final report.

## Agent Definition Standard
- Every agent definition must explicitly include:
  - Trigger
  - Input
  - Output
  - Fail condition
- Output formats must stay concrete and implementation-usable.
- Prefer fewer, sharper agents over many overlapping reviewer roles.
- Use `SKILL.md` for repeatable procedures and long checklists.
- Use `AGENTS.md` for team structure, routing, output contracts, and failure rules.

## Preferred Skills
- Use local skills under `.codex/skills` for repeatable execution procedures.
- Recommended skills in this repo:
  - `ui-review`: spacing, hierarchy, alignment, state completeness review
  - `mobile-qa`: scenario-based Android QA and regression checks
  - `product-spec`: executable spec template with acceptance criteria
  - `android-implementation`: local Compose implementation guardrails and validation
  - `reference-research`: external reference collection with fact/pattern separation
