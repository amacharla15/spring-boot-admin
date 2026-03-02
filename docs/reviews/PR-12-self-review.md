What changed and why?
I added a UI regression test for the navigation item component to enforce click affordance. I also made the affordance explicit by adding the cursor-pointer class to the sba-nav-item element so the UI consistently indicates clickability.

Why is this the right test layer (unit/integration/UI)?
This is a UI test because the behavior is purely user-facing (visual click affordance). A server-side integration test cannot catch CSS/class regressions. The test is lightweight and targets a specific DOM/CSS contract.

How to run / evidence
Command:
cd spring-boot-admin-server-ui
npm ci
npx vitest run --config vite.config.mts src/main/frontend/components/sba-nav/sba-nav-item.spec.ts
Evidence: The test fails if the cursor-pointer affordance is removed from nav items.

What could still break / what’s not covered?
This covers the sba-nav-item affordance but not every clickable element in the UI. It does not validate full navigation flows or routing, only the presence of the affordance contract.

Risks / follow-ups
A follow-up improvement would be adding similar contract tests for other primary navigation elements and ensuring lint/format checks pass in CI for the UI module.
