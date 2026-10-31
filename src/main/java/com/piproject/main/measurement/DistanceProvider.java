package com.piproject.main.measurement;

/** A measurement source; implementations must return a distance in centimeters. */
public interface DistanceProvider {
    double readDistanceCm();
}
