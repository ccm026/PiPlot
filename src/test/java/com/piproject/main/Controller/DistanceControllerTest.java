package com.piproject.main.Controller;

import com.piproject.main.measurement.DistanceProvider;
import com.piproject.main.measurement.MeasurementUnavailableException;
import org.junit.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

/** Exercises Spring MVC routing/response shape without a running server or GPIO. */
public class DistanceControllerTest {
    private MockMvc client(DistanceProvider provider) {

    InternalResourceViewResolver jspResolver =
            new InternalResourceViewResolver();

    jspResolver.setPrefix("/WEB-INF/");
    jspResolver.setSuffix(".jsp");

    return MockMvcBuilders
            .standaloneSetup(new DistanceController(provider))
            .setViewResolvers(jspResolver)
            .build();
	}

    @Test
    public void runReturnsNestedMeasurementJson() throws Exception {
        client(() -> 20.0)
            .perform(get("/graph"))
            .andExpect(status().isOk())
            .andExpect(view().name("graph"))
            .andExpect(forwardedUrl("/WEB-INF/graph.jsp"));
    }

    @Test
    public void graphStillResolvesOriginalJspViewName() throws Exception {
        client(() -> 20.0).perform(get("/graph"))
                .andExpect(status().isOk())
                .andExpect(view().name("graph"));
    }

    @Test
    public void invalidNonFiniteReadingReturns503InsteadOfInvalidJson() throws Exception {
        client(() -> Double.NaN).perform(get("/run"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("measurement_unavailable"));
    }

    @Test
    public void negativeReadingReturns503() throws Exception {
        client(() -> -1.0).perform(get("/run"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("measurement_unavailable"));
    }

    @Test
    public void failedProviderReturns503() throws Exception {
        client(() -> { throw new MeasurementUnavailableException("sensor disconnected"); })
                .perform(get("/run"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("measurement_unavailable"));
    }
}
