package com.bytex.snamp.connector.dsp.groovy;

import com.bytex.snamp.connector.dsp.NotificationParser;
import com.bytex.snamp.connector.dsp.notifications.MeasurementNotification;
import com.bytex.snamp.connector.dsp.notifications.ValueMeasurementNotification;
import com.bytex.snamp.instrumentation.measurements.BooleanMeasurement;
import com.bytex.snamp.instrumentation.measurements.IntegerMeasurement;
import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;

import javax.management.Notification;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class GroovyNotificationParserTest extends Assert {
    private final GroovyNotificationParserLoader loader;

    public GroovyNotificationParserTest() throws IOException, URISyntaxException {
        final URL resource = getClass().getClassLoader().getResource("scripts/");
        assertNotNull(resource);
        loader = new GroovyNotificationParserLoader(getClass().getClassLoader(), resource);
    }

    @Test
    public void notificationParserTest() throws Exception {
        final GroovyNotificationParser parser = loader.createScript("NotificationParser.groovy", null);
        parser.setComponentName("NAME");
        parser.setInstanceName("INSTANCE");
        final List<Notification> result = parser.parse(ImmutableMap.of("Content-Type", "application/xml"), "Body").collect(Collectors.toList());
        assertFalse(result.isEmpty());
        result.forEach(n -> {
            assertNotNull(n);
            assertEquals("application/xml", n.getUserData());
        });
    }

    @Test
    public void measurementParserTest() throws Exception {
        final NotificationParser parser = loader.createScript("MeasurementParser.groovy", null);
        final List<Notification> result = parser.parse(ImmutableMap.of("Content-Type", "application/xml"), "Body").collect(Collectors.toList());
        assertFalse(result.isEmpty());
        result.forEach(n -> {
            assertTrue(n instanceof ValueMeasurementNotification);
            assertEquals("application/xml", ((MeasurementNotification<?>)n).getMeasurement().getAnnotations().get("contentType"));
        });
    }

    @Test
    public void instantMeasurementsTest() throws Exception{
        final NotificationParser parser = loader.createScript("InstantMeasurements.groovy", null);
        final List<Notification> result = parser.parse(ImmutableMap.of("m1", new BooleanMeasurement(), "m2", new IntegerMeasurement()), "").collect(Collectors.toList());
        assertEquals(2, result.size());
        assertTrue(result.get(0) instanceof ValueMeasurementNotification);
        assertTrue(result.get(1) instanceof ValueMeasurementNotification);
    }
}
