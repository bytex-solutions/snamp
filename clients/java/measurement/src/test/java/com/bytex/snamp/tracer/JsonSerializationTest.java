package com.bytex.snamp.tracer;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * Represents JSON serialization tests.
 */
public final class JsonSerializationTest extends Assert {
    @Test
    public void intMeasurementSerialization() throws IOException {
        IntegerMeasurement measurement = new IntegerMeasurement();
        measurement.setComponentName("SNAMP");
        measurement.setInstanceName("instance1");
        measurement.setValue(42L);
        measurement.setMessage("Message");
        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(System.out, measurement);
    }
}
