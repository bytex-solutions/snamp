package com.bytex.snamp.instrumentation;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.concurrent.TimeUnit;

/**
 * Represents JSON serialization tests.
 */
public final class JsonSerializationTest extends Assert {
    @Test
    public void intMeasurementSerialization() throws IOException {
        final IntegerMeasurement original = StandardMeasurements.freeRam(100L);
        original.setDefaultComponentName();
        original.setDefaultInstanceName();
        original.getUserData().put("key", "value");
        final ObjectMapper mapper = new ObjectMapper();
        final Measurement actual = mapper.readValue(original.toJsonString(), Measurement.class);
        assertTrue(actual instanceof IntegerMeasurement);
        assertEquals(original.getValue(), ((IntegerMeasurement)actual).getValue());
        assertEquals(original.getTimeStamp(), actual.getTimeStamp());
        assertEquals(original.getUserData(), actual.getUserData());
        assertEquals(original.getChangeType(), ((ValueMeasurement) actual).getChangeType());
    }

    @Test
    public void timeMeasurementSerialization() throws IOException {
        final TimeMeasurement original = new TimeMeasurement();
        original.setName("responseTime");
        original.setDefaultComponentName();
        original.setDefaultInstanceName();
        original.setDuration(42L, TimeUnit.NANOSECONDS);
        assertEquals(new Long(42L), original.convertTo(long.class));
        original.getUserData().put("key", "value");
        final ObjectMapper mapper = new ObjectMapper();
        final StringWriter writer = new StringWriter(1024);
        mapper.writeValue(writer, original);
        final Measurement actual = mapper.readValue(writer.toString(), Measurement.class);
        writer.close();
        assertTrue(actual instanceof TimeMeasurement);
        assertEquals(original.getDuration(TimeUnit.NANOSECONDS), ((TimeMeasurement)actual).getDuration(TimeUnit.NANOSECONDS), 0.01D);
        assertEquals(original.getTimeStamp(), actual.getTimeStamp());
        assertEquals(original.getUserData(), actual.getUserData());
    }

    @Test
    public void spanSerialization() throws IOException {
        Span original = new Span();
        original.setName("dbAccess");
        original.setDefaultComponentName();
        original.setDefaultInstanceName();
        original.setDuration(42L, TimeUnit.NANOSECONDS);
        original.setSpanID(Identifier.ofString("SPAN-ID"));
        original.getUserData().put("key", "value");
        final ObjectMapper mapper = new ObjectMapper();
        final StringWriter writer = new StringWriter(1024);
        mapper.writeValue(writer, original);
        final Measurement actual = mapper.readValue(writer.toString(), Measurement.class);
        writer.close();
        assertTrue(actual instanceof Span);
        assertEquals(original.getDuration(TimeUnit.NANOSECONDS), ((TimeMeasurement)actual).getDuration(TimeUnit.NANOSECONDS), 0.01D);
        assertEquals(original.getTimeStamp(), actual.getTimeStamp());
        assertEquals(original.getUserData(), actual.getUserData());
        assertEquals(original.getSpanID(), ((Span) actual).getSpanID());
    }
}
