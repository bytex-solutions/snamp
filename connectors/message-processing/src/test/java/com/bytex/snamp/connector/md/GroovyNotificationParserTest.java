package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.md.groovy.GroovyNotificationLoader;
import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;

import javax.management.Notification;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class GroovyNotificationParserTest extends Assert {
    private final GroovyNotificationLoader loader;

    public GroovyNotificationParserTest() throws IOException, URISyntaxException {
        final URL resource = getClass().getClassLoader().getResource("scripts/NotificationParser.groovy");
        assertNotNull(resource);
        final String scriptPath = new File(resource.toURI()).getParent();
        loader = new GroovyNotificationLoader(getClass().getClassLoader(), new Properties(), scriptPath);
    }

    @Test
    public void parserTest() throws Exception {
        final NotificationParser parser = loader.createScript("NotificationParser.groovy", null);
        final Notification result = parser.parse(ImmutableMap.of("Content-Type", "application/xml"), "Body");
        assertNotNull(result);
        assertEquals("application/xml", result.getUserData());
    }
}
