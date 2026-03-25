---
name: product-spec
description: Use when turning a user request into an executable spec with scope, flow, states, events, edge cases, and acceptance criteria before implementation.
---

# Product Spec

Use this skill before implementation when the request is more than a trivial single-step code tweak.

## Trigger

Use when:
- the user asks for a feature or behavioral change
- the request is ambiguous or has multiple possible interpretations
- implementation should not begin without clear acceptance criteria

## Workflow

1. Define the user goal in one sentence.
2. Separate in-scope from out-of-scope.
3. Write the primary user flow.
4. List required states.
5. List events and transitions.
6. List edge cases and rejection conditions.
7. Write acceptance criteria that can be tested.

## Required Sections

- Goal
- In scope
- Out of scope
- User flow
- States
- Events
- Edge cases
- Acceptance criteria

## Quality Bar

- Scope must be bounded.
- States must be explicit, not implied.
- Acceptance criteria must be concrete enough for QA.
- If something is intentionally deferred, state it.

## Fail Conditions

- Suggesting features without defining states or events
- Letting scope grow without boundaries
- Passing work to implementation without testable acceptance criteria
