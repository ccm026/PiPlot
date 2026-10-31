# PiPlot Development Roadmap

**Status:** Proposed, genuine future work for a personally maintained project  
**Last updated:** 2026-10-31  
**Repository:** https://github.com/ccm026/PiPlot

## Purpose

PiPlot currently demonstrates ultrasonic-distance visualization in a browser, including a working **Windows simulation mode** without Raspberry Pi hardware. The next stage is to improve reliability, code organization, test coverage, security of dependencies, and reproducibility, while preserving the existing demonstration login unless a later design decision changes it.

This roadmap records work **not yet completed**. Estimates are planning ranges, **not time already spent or guaranteed effort**. Each task should be adjusted to match the implementation work I actually intend to perform. A task is complete only when its acceptance criteria have been demonstrated and recorded.

## Already completed — not counted as future work

- Preserved the original repository and commit history.
- Added an MIT `LICENSE` and `.gitignore`; stopped tracking generated build outputs.
- Established Java 8 / Maven 3.9.16 build environment on Windows.
- Replaced unavailable Pi4J `1.2-SNAPSHOT` with stable `1.2`.
- Added configurable Windows simulation mode (`piplot.simulation=true`) that avoids native GPIO initialization during simulation.
- Separated simulation and hardware measurement providers and introduced a 13-test Java regression suite; verified locally on Java 8.
- Added Java 8 GitHub Actions workflow; CI success is not yet claimed until the remote run is green.
- Demonstrated the existing login page, Home page, live graph, and `/run` JSON endpoint in a browser.
- Corrected integer parsing in the graph in a later commit; further numerical/error-case validation remains useful.
- Added screenshots of the working demonstration.

## Planned work summary

| ID | Workstream | Estimated remaining effort | Priority | Status |
| --- | --- | ---: | --- | --- |
| PP-01 | Stabilize GPIO acquisition and sensor fault handling | 6–9 h | High | Planned |
| PP-02 | Separate measurement providers from web controllers | Completed (not counted) | High | Implemented; hardware unvalidated |
| PP-03 | Improve plotting, polling, and API error handling | 4–6 h | High | Planned |
| PP-04 | Expand automated regression tests | 3–5 h | High | 13 software-only tests added; more work planned |
| PP-05 | Review and modernize legacy dependencies | 6–10 h | Medium | Planned |
| PP-06 | Improve CI reliability and hardware documentation | 2–4 h | Medium | Workflow added, green CI run pending |
| **Total** | **Estimated remaining work** | **21–34 h** | | |

> **Planning caution:** Some tasks may overlap and estimates can change after investigation. Do not claim the full range as completed, and do not create artificial tasks merely to reach a target number of hours. The scope and actual work should remain genuine.

---

## PP-01 — Stabilize GPIO acquisition and sensor fault handling

**Estimate:** 6–9 hours  
**Priority:** High  
**Area:** `DistanceController.java` and future hardware-acquisition code

### Problem

The physical measurement path currently provisions GPIO during each request, waits for echo transitions without timeouts, and shuts down the GPIO controller after a reading. The trigger pulse uses a sleep expression that does not generate the intended microsecond-scale delay. These limitations can cause blocked requests, unstable repeated measurements, and resource errors.

### Planned work

- Define an explicit GPIO lifecycle so pins are initialized and released predictably.
- Replace unbounded echo polling with bounded waits and meaningful timeout outcomes.
- Implement correct trigger-pulse timing for the chosen sensor and platform.
- Make cleanup reliable when measurement or interruption fails.
- Establish behavior for disconnected sensors, invalid echo intervals, and concurrent requests.

### Acceptance criteria

- A missing or stuck echo completes with a bounded failure rather than hanging indefinitely.
- Repeated measurements do not repeatedly shut down shared GPIO infrastructure incorrectly.
- Trigger timing and distance conversion are explained and covered by suitable tests where feasible.
- Actual Raspberry Pi validation is documented **if compatible hardware is available**; otherwise, hardware-specific acceptance remains open and is not marked complete.

## PP-02 — Separate measurement providers from web controllers

**Status:** Completed for the software-only rework (not included in remaining-hour estimates)  
**Priority:** High  
**Area:** Controller and service architecture

### Problem

Originally, the HTTP controller combined GPIO-specific logic and simulation logic. This software rework extracted them into `HardwareDistanceProvider` and `SimulationDistanceProvider`, selected by `DistanceProviderConfiguration`. Physical GPIO reliability remains unvalidated.

### Planned work

- Introduce a small measurement-provider abstraction (for example, `DistanceSource`).
- Move Raspberry Pi acquisition into a hardware provider and the existing generator into a simulation provider.
- Keep source selection explicit through application configuration.
- Return a consistent success or error result from the provider layer.
- Preserve existing web endpoints while refactoring.

### Acceptance criteria

- Windows simulation starts without loading native GPIO functions.
- The controller no longer directly contains the sensor acquisition algorithm.
- Provider implementations can be tested or substituted independently.
- The existing demo login, Home page, and graph navigation remain operational.

## PP-03 — Improve plotting, polling, and API error handling

**Estimate:** 4–6 hours  
**Priority:** High  
**Area:** `graph.jsp`, `/run` response handling

### Problem

The chart demonstrates successful simulated updates, but frontend handling remains fragile: overlapping requests, unreachable endpoints, malformed data, and script dependency loading need better behavior. The latest commit addresses integer parsing; precision still needs explicit regression checks.

### Planned work

- Validate numeric extraction (including fractional distances) against the actual JSON structure.
- Prevent overlapping polling requests and report network or measurement errors without breaking the page.
- Define sensible chart update rates, buffer limits, and empty-state behavior.
- Consolidate duplicate jQuery imports and replace insecure HTTP script references with appropriate trusted HTTPS sources or local assets.
- Review CanvasJS usage and licensing; preserve a working visualization or choose an appropriately licensed alternative as warranted.
- Consider improving the measurement API with units, timestamps, source identifiers, and structured errors while maintaining compatibility.

### Acceptance criteria

- Fractional readings remain fractional in the displayed data.
- A failed or malformed response does not add a bogus point or stop future updates.
- Slow network responses do not create uncontrolled overlapping requests.
- The demo graph continues functioning in Windows simulation mode.
- Frontend asset sources and applicable licensing are documented.

## PP-04 — Implement meaningful automated regression tests

**Estimate:** 3–5 hours remaining  
**Priority:** High  
**Area:** `src/test/` and build verification

### Problem

A 13-test software-only suite now covers deterministic simulation values, provider selection, successful JSON responses, negative and invalid measurements, provider unavailability, and JSP view mapping. Remaining work includes more edge conditions and frontend/error regressions; a green GitHub CI run is also required.

### Planned work

- Add tests for simulated measurements, range/precision expectations, and source selection.
- Add focused tests for distance calculations, timing conversion, and hardware timeout/error handling using test doubles.
- Verify response JSON shape and HTTP error outcomes.
- Add regression checks for repeated measurements and controller behavior.
- Keep hardware-dependent tests separated from software-only tests so CI can run without Raspberry Pi hardware.

### Acceptance criteria

- New tests execute under `mvn test` and exercise meaningful behavior rather than just compilation.
- No Raspberry Pi or live sensor is needed for the regular test suite.
- Failures have clear diagnostics and are reproducible.
- Test instructions are documented in the README or developer notes.

## PP-05 — Review and modernize legacy dependencies

**Estimate:** 6–10 hours initially; may require a larger follow-up project  
**Priority:** Medium  
**Area:** `pom.xml`, Spring MVC/JSP integration, Pi4J and browser dependencies

### Problem

The project uses Spring Boot 1.5.6 and manually pinned dependencies from different Spring/Tomcat generations. Although the initial Windows demonstration works with JDK 8 and Maven 3.9.16, the dependency set is not suitable for a new production deployment.

### Planned work

- Inventory direct and transitive dependencies and identify redundant/conflicting declarations.
- Evaluate a manageable upgrade path, keeping the web interface operational at each stage.
- Remove no-longer-needed snapshot repository configuration if dependencies no longer require it.
- Review Pi4J API/platform compatibility before attempting major-version changes.
- Document any remaining third-party limitations or security risks.

### Acceptance criteria

- A dependency inventory and rationale for removals/upgrades are recorded.
- Changes are made incrementally and verified with a build and browser smoke test.
- No untested claim is made that current Raspberry Pi hardware remains supported after changes.
- If the full modernization exceeds the estimate, split remaining migration work into a separately scoped issue.

## PP-06 — Add CI, reproducible setup, and hardware documentation

**Estimate:** 2–4 hours remaining  
**Priority:** Medium  
**Area:** `.github/workflows/`, README, `docs/`

### Problem

The application has a documented Windows simulation setup and an initial Java 8 GitHub Actions workflow, but CI needs a confirmed green run and broader reproducibility and hardware documentation remain future work.

### Planned work

- Verify GitHub Actions runs Maven `clean verify` on Java 8, and address any Linux-only dependency or CI configuration failures.
- Record tested Java/Maven versions and known limitations of JSP packaging.
- Document Raspberry Pi GPIO logical pin mapping versus physical header pin numbers.
- Include sensor voltage-level precautions, required hardware, and a troubleshooting guide.
- Document how to enable simulation without hardware and how to recognize API failures.

### Acceptance criteria

- CI runs the intended build/tests without Pi hardware, and its result is visible on GitHub.
- A new developer can follow the Windows simulation instructions from a clean checkout.
- Hardware documentation clearly distinguishes verified connections from unverified guidance.
- Screenshots and documentation match the actual code and UI.

---

## Proposed work order

1. **PP-02 is completed** for the software-only rework; physical GPIO behavior is not validated.
2. **PP-06:** obtain a green GitHub Actions Java 8 verification run and finish reproducibility notes.
3. **PP-04:** extend tests beyond the initial software-only 13-test suite.
4. **PP-01:** fix hardware timing, lifecycle, and bounded failure handling when suitable hardware is available.
5. **PP-03:** improve frontend behavior and measurement API resilience.
6. **PP-05:** modernize dependencies incrementally after baseline behavior is protected by tests.

Some workstreams will overlap. The order is guidance, not a requirement to finish each one before starting the next.

## How progress will be recorded

- Create a GitHub Issue for each `PP-0x` workstream and link related commits or pull requests.
- Use issue descriptions with the problem statement, implementation boundaries, and acceptance criteria.
- Update this roadmap only when a task's status actually changes.
- Keep the original Git history intact; do not rewrite it to fabricate activity.
- Record observed build/test commands, platform, and outcomes accurately.

## Non-goals for this stage

- No claim that physical sensor measurements have been revalidated on present-day hardware.
- No claim that the single-account demo login provides access control.
- No claim that all dependency vulnerabilities have already been resolved.
- No conversion of this Java project into the separate Python/PyQt oscilloscope concept.

## Definition of done

PiPlot can be considered substantially modernized when it has reliable, bounded measurement handling; cleanly separated hardware and simulation sources; stable browser error handling; meaningful automated tests; a documented and supportable dependency baseline; and reproducible CI. Physical-hardware claims require actual physical-hardware validation.
