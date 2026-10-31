package com.piproject.main.measurement;

public class MeasurementUnavailableException extends RuntimeException {
    public MeasurementUnavailableException(String message) {
        super(message);
    }

    public MeasurementUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
