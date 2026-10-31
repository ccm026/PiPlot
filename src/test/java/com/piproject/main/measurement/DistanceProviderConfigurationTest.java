package com.piproject.main.measurement;

import static org.junit.Assert.*;
import java.util.Collections;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;

/** Tests provider selection without touching physical GPIO hardware. */
public class DistanceProviderConfigurationTest {
    private DistanceProvider select(String simulationValue) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        if (simulationValue != null) {
            context.getEnvironment().getPropertySources().addFirst(new MapPropertySource(
                    "test", Collections.<String, Object>singletonMap("piplot.simulation", simulationValue)));
        }
        context.register(DistanceProviderConfiguration.class);
        context.refresh();
        try {
            assertEquals(1, context.getBeanNamesForType(DistanceProvider.class).length);
            return context.getBean(DistanceProvider.class);
        } finally {
            context.close();
        }
    }

    @Test
    public void trueSelectsSimulation() {
        assertTrue(select("true") instanceof SimulationDistanceProvider);
    }

    @Test
    public void falseSelectsHardwareWithoutConnectingToGpio() {
        assertTrue(select("false") instanceof HardwareDistanceProvider);
    }

    @Test
    public void missingSettingDefaultsToHardware() {
        assertTrue(select(null) instanceof HardwareDistanceProvider);
    }
}
