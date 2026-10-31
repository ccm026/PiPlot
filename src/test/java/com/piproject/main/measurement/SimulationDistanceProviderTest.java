package com.piproject.main.measurement;

import static org.junit.Assert.*;
import org.junit.Test;

public class SimulationDistanceProviderTest {
    @Test
    public void producesKnownReadingAtZeroTime() {
        assertEquals(20.0, new SimulationDistanceProvider(() -> 0L).readDistanceCm(), 0.0001);
    }

    @Test
    public void oscillatesAroundTwentyCentimeters() {
        assertEquals(30.0, new SimulationDistanceProvider(() -> 1571L).readDistanceCm(), 0.01);
        assertEquals(10.0, new SimulationDistanceProvider(() -> 4712L).readDistanceCm(), 0.01);
    }

    @Test
    public void repeatsSameValueAtSameTime() {
        SimulationDistanceProvider provider = new SimulationDistanceProvider(() -> 2000L);
        assertEquals(provider.readDistanceCm(), provider.readDistanceCm(), 0.0);
    }

    @Test
    public void staysFiniteWithinRangeAndTwoDecimals() {
        for (long millis = 0; millis <= 30000; millis += 97) {
            final long time = millis;
            double distance = new SimulationDistanceProvider(() -> time).readDistanceCm();
            assertTrue(Double.isFinite(distance));
            assertTrue(distance >= 10.0 && distance <= 30.0);
            assertEquals(distance * 100.0, Math.rint(distance * 100.0), 1e-8);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNullClock() {
        new SimulationDistanceProvider(null);
    }
}
