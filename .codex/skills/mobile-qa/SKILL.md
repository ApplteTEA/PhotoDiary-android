---
name: mobile-qa
description: Use when validating Android app changes and you need a repeatable QA checklist covering happy path, edge cases, navigation, restoration, keyboard, permissions, and regression risk.
---

# Mobile QA

Use this skill after implementation and before calling a change finished.

## Trigger

Use when:
- Android UI or behavior changed
- the task affects save, edit, navigation, dialogs, permissions, images, or keyboard behavior
- you need a pass/fail style review

## Workflow

1. Verify the main happy path.
2. Verify empty and partial-data states.
3. Verify error and interruption paths when relevant.
4. Verify navigation and back behavior.
5. Verify restoration and repeat-entry behavior when relevant.
6. Summarize pass/fail with repro steps for failures.

## Checklist

- Happy path:
  Can the user complete the intended task start to finish?
- Empty state:
  What happens with no data, no title, no content, no image, no tag?
- Partial state:
  What happens when only some fields are filled?
- Back/navigation:
  Back press, up button, tab change, dialog dismiss, cancel flow.
- Restoration:
  Edit existing data, reopen the same screen, revisit after save, unsaved-exit flow.
- Keyboard:
  Initial focus, overlap, scroll, CTA obstruction, IME padding.
- Permissions and media:
  Photo picker, preview, delete, denied or empty outcomes if relevant.
- Regression:
  Nearby screens or shared components affected by the same change.

## Output Format

- Result: Pass/Fail
- Functional QA
- UX QA
- Regression risks
- Required fixes

## Fail Conditions

- Only checking happy path
- Missing back, restore, or partial-data checks
- No repro steps for failures
- Reporting “looks fine” without scenario-based validation
