---
name: ui-review
description: Use when reviewing or revising UI/UX in this app and you need a strict checklist for spacing, hierarchy, alignment, state completeness, and awkward visual grammar before calling work finished.
---

# UI Review

Use this skill for screen polish, redesign feedback, or before reporting UI work as complete.

## Trigger

Use when:
- the task changes visible UI
- the user says a screen looks weird, off, awkward, crowded, or unfinished
- you need a final self-check before reporting UI work done

## Workflow

1. Identify the primary screen purpose.
2. List the first thing the user should notice.
3. Check whether spacing, hierarchy, and alignment support that purpose.
4. Check whether the same information level uses the same visual grammar.
5. Check whether empty, loading, error, selected, and disabled states exist when relevant.
6. Only then propose or implement fixes.

## Mandatory Checks

- Spacing jumps:
  Look for sudden large or small gaps between adjacent sections.
- Alignment:
  Compare text start lines, icon baselines, chip rows, and container padding.
- Hierarchy:
  Verify title, meta, body, CTA, helper text are not competing at the same level.
- Component grammar:
  Avoid one screen mixing card, pill, flat-row, and boxed-panel patterns without a strong reason.
- Overgrown UI:
  Remove labels, helper text, outlines, shadows, or containers that do not earn their space.
- Box-inside-box:
  Reject nested surfaces unless they communicate real structure.
- State completeness:
  Check empty, error, loading, success, and edge states when applicable.

## Output Format

- Current impression
- Immediate awkward points
- Why they are wrong
- Fix direction
- Remaining risks

## Fail Conditions

- Vague comments like “make it prettier”
- No concrete file-level or component-level fix direction
- Ignoring empty or error states on screens that need them
- Approving a UI that still has obvious spacing or hierarchy issues
