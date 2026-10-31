package com.piproject.main.Controller;

import com.piproject.main.measurement.DistanceProvider;
import com.piproject.main.measurement.MeasurementUnavailableException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/** Web endpoints; all measurement sources use one stable JSON contract. */
@Controller
public class DistanceController {
    private final DistanceProvider provider;

    public DistanceController(DistanceProvider provider) {
        this.provider = provider;
    }

    @RequestMapping(value = "/graph", method = RequestMethod.GET)
    public String graph() {
        return "graph";
    }

    @RequestMapping(value = "/run", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<String> run() {
        try {
            double reading = provider.readDistanceCm();
            if (!Double.isFinite(reading) || reading < 0.0) {
                throw new MeasurementUnavailableException("Invalid sensor reading");
            }
            JSONArray samples = new JSONArray().put(new JSONArray().put(reading));
            JSONObject result = new JSONObject().put("measuredData", samples);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                    .body(result.toString());
        } catch (MeasurementUnavailableException ex) {
            // Stable machine-readable error; do not return bogus measurements.
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\":\"measurement_unavailable\"}");
        }
    }
}
