PR-10 Self Review (Solo)
1) What changed and why?

In this PR I added a new integration test class, InstancesRegistrationSecurityIntegrationTest, under the sample servlet module to lock down the security contract for instance registration at POST /instances when the application is running under the secure profile.

Why this change matters:

POST /instances is a critical entry point into Spring Boot Admin: clients register themselves here so the admin server can monitor them.

This endpoint is security-sensitive:

If it accidentally becomes open, any actor could register arbitrary instances (noise, spoofing, or abuse).

If it accidentally becomes blocked, legitimate clients cannot register and Spring Boot Admin becomes unusable for monitoring.

Without a test contract, small changes to Spring Security configuration, CSRF behavior, or auth entry points can silently change the behavior (for example switching between 401/302, or allowing unauthenticated registration).

What I implemented:

A test that verifies the endpoint requires authentication when secure profile is active:

No credentials ⇒ registration request is rejected (401).

Valid credentials (user/password) ⇒ registration is accepted (201 Created).

This PR is intentionally scoped to only the behavior described in the issue acceptance criteria: security contract for /instances registration under secure profile.

2) Why is this the right test layer (unit vs integration vs UI)?

This should be an integration test, not a unit test and not a UI test.

Not unit test:

The behavior is produced by Spring Security’s real filter chain wiring (authentication entry point, authorization rules, CSRF exceptions for POST /instances, and HTTP basic configuration).

A unit test would typically mock security components or bypass the filter chain, which would miss the real wiring and would not reliably detect a regression in the actual HTTP behavior.

Not UI test:

The contract is an API/security behavior. The UI does not exercise the registration endpoint in a way that proves the contract.

Integration test is correct because:

It validates the full runtime contract over HTTP using WebTestClient.

It exercises the real Spring Security pipeline and ensures the response status code matches expectations.

It’s the highest-signal regression protection for security behavior.

3) How to run / evidence (exact commands and what “passing” means)

Run the test from repo root (Windows PowerShell):

.\mvnw.cmd -pl spring-boot-admin-samples/spring-boot-admin-sample-servlet -Dtest=InstancesRegistrationSecurityIntegrationTest test

What “passing” proves:

When running the app in secure configuration (as used by the test):

POST /instances without auth returns 401 Unauthorized

POST /instances with valid Basic auth (user / password) returns 201 Created

Evidence of regression protection:

If someone changes Spring Security rules and accidentally permits unauthenticated registration, the first assertion fails.

If someone changes auth wiring (credentials, auth mechanism, entry point) and breaks successful registration, the second assertion fails.

If someone changes CSRF exceptions for /instances such that POST starts failing unexpectedly, the “with auth” case would fail and highlight the regression.

4) What could still break / what is NOT covered?

This PR only covers a specific slice of the system:

Covered:

The registration authentication contract for POST /instances under the secure profile used by the test.

Not covered (intentional, out of scope for this PR):

Proxy endpoints (/instances/{id}/actuator/** and related) — separate issue (#9).

UI login flows or UI-level authorization behavior.

Other runtime modes/modules (reactive sample, WAR sample, cloud discovery samples).

Specific error-body JSON structure (message fields) — only the status code contract is enforced.

Variants like 403 vs 401 under different entry-point setups. The current contract is pinned to the observed behavior in secure configuration.

Potential behavior drift that may require future adjustment:

If the project intentionally changes the auth entry point behavior (for example returning 302 redirect to /login instead of 401 for unauthenticated access), the test would fail even though auth is still required. In that case the test would need to be updated to enforce the security requirement (“auth required”) while matching the new intended status-code contract.

5) Risks / follow-ups / next steps

Risks introduced by this PR:

Minimal. This PR adds tests and does not change production behavior.

The main risk is test brittleness if security behavior intentionally changes; that’s acceptable because the goal is to force explicit decisions about API security behavior.

Follow-ups planned (already tracked in backlog):

Issue #9: Add/strengthen integration tests for InstancesProxyController proxying behavior (status/body/header forwarding and ignored header filtering).

Issue #7: Add a UI Vitest contract test for click affordance (cursor-pointer) for navigation elements (to prevent silent UX regression).

6) Why this improves regression safety (one clear sentence)

This PR turns the /instances registration security expectation into an executable contract so that accidental “open registration” or accidental “registration lockout” regressions are caught immediately by tests and CI.
