package com.bytex.snamp.connector.md.groovy;

import com.bytex.snamp.connector.md.NotificationParser;
import com.bytex.snamp.connector.md.notifications.MeasurementNotification;
import com.bytex.snamp.connector.md.notifications.ValueMeasurementNotification;
import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;

import javax.management.Notification;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

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
        final Notification result = parser.parse(ImmutableMap.of("Content-Type", "application/xml"), "Body");
        assertNotNull(result);
        assertEquals("application/xml", result.getUserData());
    }

    @Test
    public void measurementParserTest() throws Exception {
        final NotificationParser parser = loader.createScript("MeasurementParser.groovy", null);
        final Notification result = parser.parse(ImmutableMap.of("Content-Type", "application/xml"), "Body");
        assertTrue(result instanceof ValueMeasurementNotification);
        assertEquals("application/xml", ((MeasurementNotification<?>)result).getMeasurement().getUserData().get("contentType"));
    }
}
