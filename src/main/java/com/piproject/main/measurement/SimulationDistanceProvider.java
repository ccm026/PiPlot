package com.piproject.main.measurement;

import java.util.function.LongSupplier;

/** Reproduces PiPlot's existing 10-30 cm sine-wave simulation. */
public class SimulationDistanceProvider implements DistanceProvider {
    private final LongSupplier milliseconds;

    public SimulationDistanceProvider() {
        this(System::currentTimeMillis);
    }

    // Test seam: a fixed clock makes simulation tests deterministic.
    public SimulationDistanceProvider(LongSupplier milliseconds) {
        if (milliseconds == null) {
            throw new IllegalArgumentException("milliseconds must not be null");
        }
        this.milliseconds = milliseconds;
    }

    @Override
    public double readDistanceCm() {
        double distance = 20.0 + 10.0 * Math.sin(milliseconds.getAsLong() / 1000.0);
        return Math.round(distance * 100.0) / 100.0;
    }
}
