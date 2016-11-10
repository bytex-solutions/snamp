package com.bytex.snamp.instrumentation;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

/**
 * Represents JSON serialization tests.
 */
public final class JsonSerializationTest extends Assert {
    @Test
    public void intMeasurementSerialization() throws IOException {
        IntegerMeasurement original = new IntegerMeasurement();
        original.setDefaultComponentName();
        original.setDefaultInstanceName();
        original.setValue(42L);
        original.setMessage("Message");
        original.getUserData().put("key", "value");
        final ObjectMapper mapper = new ObjectMapper();
        final StringWriter writer = new StringWriter(1024);
        mapper.writeValue(writer, original);
        final Measurement actual = mapper.readValue(writer.toString(), Measurement.class);
        writer.close();
        assertTrue(actual instanceof IntegerMeasurement);
        assertEquals(original.getValue(), ((IntegerMeasurement)actual).getValue());
        assertEquals(original.getMessage(), actual.getMessage());
        assertEquals(original.getTimeStamp(), actual.getTimeStamp());
        assertEquals(original.getUserData(), actual.getUserData());
    }
}
