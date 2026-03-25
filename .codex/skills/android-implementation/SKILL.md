---
name: android-implementation
description: Use when implementing Android/Compose changes in this project and you need the local guardrails for minimal invasive edits, shared UI behavior, validation, and reporting.
---

# Android Implementation

Use this skill for code changes in this Android project.

## Trigger

Use when:
- editing Compose UI or state logic
- implementing behavior already defined by planning or design
- touching shared screen patterns or reusable UI pieces

## Project Guardrails

- Prefer small, local changes over broad refactors.
- Preserve edge-to-edge behavior.
- Do not add dark mode.
- Keep Gradle and Android Studio sync stable.
- Do not widen scope during implementation.

## Workflow

1. Read the target files and nearby shared components.
2. Identify the smallest safe edit surface.
3. Implement only agreed behavior.
4. Run validation.
5. Report changed files, what changed, what did not change, risks, and QA checks.

## Validation

- Preferred compile check:
  `JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home ./gradlew :app:compileDebugKotlin`
- Use install or run checks when the task requires device confirmation.

## UI Guardrails

- Match shared padding and alignment rules across related screens.
- Avoid adding new card, pill, or container styles unless required.
- Treat “looks weird” as a correctness bug.
- Do not leave empty placeholder metadata visible on read-only screens unless intentional.

## Fail Conditions

- Refactoring beyond the requested scope
- Implementing design decisions that were not agreed
- Skipping validation
- Reporting completion without changed-file and risk summary
