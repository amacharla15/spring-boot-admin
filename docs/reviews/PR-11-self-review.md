What changed and why?
I strengthened the existing InstancesProxyController integration tests to add explicit regression coverage for proxy behavior that can silently break the Spring Boot Admin UI. I added an assertion that proxied responses preserve Content-Type, and I added verification that hop-by-hop headers are filtered and not forwarded to the managed instance. This matters because proxy behavior is fragile and small changes in header filtering or response handling can break actuator rendering in the UI without obvious compile-time failures.

Why is this the right test layer (unit/integration/UI)?
This is the right layer as an integration test because it validates the full proxy pipeline over HTTP (controller routing + HttpHeaderFilter behavior + downstream HTTP call + response relay) using WebTestClient and a deterministic WireMock backend. A unit test would not reliably catch real forwarding behavior or header filtering across the HTTP boundary, and a UI test would be too indirect to pinpoint proxy contract regressions.

How to run / evidence
Command: .\mvnw.cmd -pl spring-boot-admin-server -Dtest=*InstancesProxyControllerIntegrationTest test
Evidence: The tests now assert Content-Type passthrough for a proxied actuator response and verify that a hop-by-hop header (X-Application-Context) is not forwarded to the downstream WireMock server. If either behavior regresses, the integration test fails immediately.

What could still break / what’s not covered?
This change focuses on Content-Type passthrough and one explicit “filtered header not forwarded” case. It does not fully cover all sensitive headers, streaming/SSE edge cases, large payload handling, unusual status-code mappings, redirects, or the entire matrix of proxy endpoints and actuator media types. More coverage could add additional filtered-header cases and different downstream content-type/status combinations.

Risks / follow-ups
This PR is low risk because it only strengthens tests and does not change production proxy logic. Follow-up work is Issue #7 (UI cursor:pointer Vitest contract) to improve frontend regression resistance and ensure navigation affordance remains consistent.
