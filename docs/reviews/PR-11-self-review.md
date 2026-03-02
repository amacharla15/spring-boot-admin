What changed and why?
I strengthened the existing InstancesProxyController integration tests to add explicit regression coverage for proxy behavior that can silently break the Spring Boot Admin UI. I added an assertion that the proxied response preserves Content-Type, and I added a test that verifies hop-by-hop headers are filtered and not forwarded to the managed instance.

Why is this the right test layer (unit/integration/UI)?
This is the right layer as an integration test because it validates the full proxy pipeline over HTTP (controller + header filtering + downstream call) using WebTestClient and a deterministic WireMock backend. A unit test would not reliably catch real forwarding behavior or header filtering across the HTTP boundary.

How to run / evidence
Command: .\mvnw.cmd -pl spring-boot-admin-server -Dtest=*InstancesProxyControllerIntegrationTest test
Evidence: The tests now assert Content-Type passthrough for a proxied actuator response and verify that a hop-by-hop header (X-Application-Context) is not forwarded to the downstream WireMock server.

What could still break / what’s not covered?
This change focuses on status/body/content-type and one filtered header case. It does not fully cover all sensitive headers, streaming edge cases, large payloads, or all proxy endpoints. Additional coverage could add more header cases and different downstream status/content-type combinations.

Risks / follow-ups
Follow-up work is Issue #7 (UI cursor:pointer Vitest contract) to improve frontend regression resistance.
