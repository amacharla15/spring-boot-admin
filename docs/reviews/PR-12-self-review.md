What changed and why?
I added a UI regression test for the navigation item component (sba-nav-item) to enforce a “click affordance” contract so that clickable navigation elements visually look clickable. To make the behavior deterministic and not dependent on browser-specific computed-style behavior, I also made the affordance explicit in the component by adding the cursor-pointer class to the sba-nav-item element. This prevents a quiet UX regression where items are still clickable, but users get no visual cue (cursor stays default), which reduces usability and is easy to miss in code review.

Why is this the right test layer (unit/integration/UI)?
This is the correct layer as a UI test because the behavior is user-facing and presentation-specific (DOM + CSS/class contract). A server-side integration test cannot detect CSS/class regressions, and even an API contract test wouldn’t help because the backend could be perfectly correct while the UI becomes confusing. The test is intentionally lightweight: it renders the component and checks a stable affordance contract (presence of a cursor-pointer indicator) rather than trying to simulate full navigation routing or end-to-end flows.

How to run / evidence
Commands:
cd spring-boot-admin-server-ui
npm ci
npx vitest run --config vite.config.mts src/main/frontend/components/sba-nav/sba-nav-item.spec.ts

Evidence:
The test fails if the cursor-pointer affordance is removed (either by deleting the cursor-pointer class or by removing the styling that indicates pointer behavior). With the change applied, the test passes locally and in CI, demonstrating that the navigation item consistently exposes the intended “this is clickable” cue.

What could still break / what’s not covered?
This change enforces the affordance contract specifically for the sba-nav-item component. It does not cover:

every clickable element across the UI (buttons, list rows, cards, etc.)

end-to-end navigation flows (routing correctness, page transitions, active state)

accessibility cues beyond cursor affordance (keyboard focus states, ARIA roles, tabindex expectations)

So the scope is intentionally narrow: it locks down one high-visibility regression risk in the primary navigation building block without turning into a large UI testing initiative.

Risks / follow-ups
Risk is low because the change is additive and limited to UI-level behavior: a small component class update plus one focused unit-style UI test. Follow-ups that would further improve regression safety:

Add similar “affordance contract” tests for other top-level navigation components (navbar items, instance/application list actions).

Ensure UI lint/format remains CI-safe for new tests (run lint/format locally before pushing, and keep the test file compliant with the repo’s prettier/eslint rules).
