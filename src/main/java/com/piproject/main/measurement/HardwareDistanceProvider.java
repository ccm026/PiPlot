package com.piproject.main.measurement;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

/**
 * Legacy GPIO path, preserved for compatible Raspberry Pi hardware.
 * This implementation is deliberately NOT exercised by CI. Timeout and
 * trigger-timing improvements are separate future hardware tasks.
 */
public class HardwareDistanceProvider implements DistanceProvider {
    @Override
    public double readDistanceCm() {
        // Initialization must remain inside this method: it fails on Windows CI.
        GpioController gpio = GpioFactory.getInstance();
        GpioPinDigitalOutput trigger = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00);
        GpioPinDigitalInput echo = gpio.provisionDigitalInputPin(
                RaspiPin.GPIO_02, PinPullResistance.PULL_DOWN);
        try {
            Thread.sleep(2000);
            trigger.high();
            Thread.sleep((long) 0.01); // Legacy timing; hardware correction is future work.
            trigger.low();

            while (echo.isLow()) { /* legacy: no timeout */ }
            long startTime = System.nanoTime();
            while (echo.isHigh()) { /* legacy: no timeout */ }
            long endTime = System.nanoTime();

            double distance = (((endTime - startTime) / 1e3) / 2) / 29.1;
            return (float) distance; // preserve legacy measurement precision
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new MeasurementUnavailableException("Hardware acquisition interrupted", ex);
        } finally {
            trigger.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
            echo.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
            gpio.shutdown();
            gpio.unprovisionPin(trigger);
            gpio.unprovisionPin(echo);
        }
    }
}
