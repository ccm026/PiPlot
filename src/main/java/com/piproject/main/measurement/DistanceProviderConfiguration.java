package com.piproject.main.measurement;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Selects only one source; CI never initializes Raspberry Pi GPIO. */
@Configuration
public class DistanceProviderConfiguration {
    @Bean
    @ConditionalOnProperty(name = "piplot.simulation", havingValue = "true")
    public DistanceProvider simulationDistanceProvider() {
        return new SimulationDistanceProvider();
    }

    @Bean
    @ConditionalOnProperty(name = "piplot.simulation", havingValue = "false", matchIfMissing = true)
    public DistanceProvider hardwareDistanceProvider() {
        return new HardwareDistanceProvider();
    }
}
