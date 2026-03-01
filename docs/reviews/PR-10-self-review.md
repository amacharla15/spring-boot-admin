What changed and why?
I added InstancesRegistrationSecurityIntegrationTest in the sample servlet module to enforce the /instances registration authentication contract under the secure profile. This protects against regressions where instance registration becomes accidentally open or becomes accidentally blocked due to security config changes.

Why is this the right test layer (unit/integration/UI)?
This is an integration test because it validates real Spring Security behavior (filter chain + auth entry point) over HTTP using WebTestClient, which is the highest-signal protection for auth contracts. A unit test would miss real security wiring and default entry point behavior.

How to run / evidence
Command: .\mvnw.cmd -pl spring-boot-admin-samples/spring-boot-admin-sample-servlet -Dtest=InstancesRegistrationSecurityIntegrationTest test
Evidence: without credentials → 401; with user/password via Basic auth → 201.

What could still break / what’s not covered?
This PR only covers registration auth for /instances in the sample servlet app’s secure profile. It does not cover proxy endpoints, UI login flows, or other profiles/modules. If the security entry point behavior changes (e.g., redirect to login instead of 401), the test may need to be adjusted while still enforcing “auth required.”

Risks / follow-ups
Next work is Issue #9 (proxy forwarding integration tests) and Issue #7 (UI cursor:pointer Vitest contract test).
