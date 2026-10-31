# CC-CLI review rework: simulation, API and CI

## Scope
This is a bounded software-only task. Existing login and JSP views remain unchanged. Raspberry Pi sensor hardware is not required in tests. The legacy GPIO algorithm has been moved into `HardwareDistanceProvider` but has **not** been revalidated or modernized.

## Changes

- `DistanceProvider` defines a measurement-source interface.
- `SimulationDistanceProvider` reproduces the current 10–30 cm sine-wave simulation with an injectable clock, permitting deterministic tests.
- `DistanceProviderConfiguration` selects simulation when `piplot.simulation=true` and the legacy hardware provider otherwise; creating the hardware provider does not access GPIO.
- `/run` retains `{"measuredData":[[value]]}` on success. Invalid readings and declared unavailable-source failures return HTTP 503 and `{"error":"measurement_unavailable"}` instead of an invalid measurement.
- Tests cover deterministic sample values, numerical range/precision, provider selection, HTTP JSON shape, graph view mapping, and failure handling.
- GitHub Actions CI runs Java 8 Maven `clean verify` without physical Raspberry Pi hardware.

## Commands

On Windows Git Bash with Java 8 and Maven available:

```bash
cd /e/PiPlot/PiPlot
mvn clean verify
mvn spring-boot:run
```

With `piplot.simulation=true` in `application.properties`, visit:

- `http://localhost:9192/` (existing demo login)
- `http://localhost:9192/run` (simulation JSON)
- `http://localhost:9192/graph` (existing graph)

In GitHub: **Actions → Java CI** should show a green run after pushing this change. Do not claim CI succeeds until the run actually passes.

## Out of scope / limitations

- Real Raspberry Pi timing, native GPIO cleanup and sensor timeouts remain untested. They should be improved in a separate hardware-focused task.
- Existing frontend graph behavior and its JavaScript dependencies are unchanged.
- Spring Boot 1.5.6 is legacy; a dependency upgrade is a separate scoped task.
- The trial login is not a secure access-control mechanism.

## Acceptance criteria

1. `mvn clean verify` passes on Java 8 locally and on GitHub Actions.
2. Tests run on a machine without GPIO hardware and cover simulation, provider-selection and `/run` behavior.
3. `/run` success JSON shape remains backward-compatible with the existing graph.
4. Provider failures never yield fabricated successful readings.
5. Login, home and graph pages still work in Windows simulation mode.
