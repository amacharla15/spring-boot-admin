# PR Template

Closes #<issue-number>

Summary
- __
- __

Verification
- tests/build run:
- manual checks:

Evidence
- Sonar rule cleared / before-after explanation:
- affected file(s):

Self-review (solo project)
- Reviewed changed files for scope control and unintended churn
- Checked acceptance criteria against the issue
- Verified tests/build/manual checks listed above


# Final Issue Time Log Comment

Time Log (required)

Triage/Understand: __
Plan: __
Implement: __
Verify: __
PR overhead: __
Review time (as reviewer): __
Rework after review: __
Total: __

Notes:
- __
- __


# Narrowing comment for #15

Implementation note for Part 2:
This issue will be completed in a small reviewable PR focused first on the TODO in spring-boot-admin-docs/pom.xml.
Any other TODO-related cleanup will be deferred or tracked separately if needed.


# Narrowing comment for #13

Implementation note for Part 2:
This issue will be completed as a small extraction from services/instance.ts rather than a large full-module rewrite, to keep the PR reviewable and aligned with the 10-minute review rule.
