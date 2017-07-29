package com.bytex.snamp.connector.dataStream.groovy;

import com.bytex.snamp.connector.notifications.NotificationBuilder;
import org.junit.Assert;
import org.junit.Test;

import javax.management.Notification;
import javax.management.NotificationFilter;
import java.io.IOException;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class GroovyNotificationFilterTest extends Assert {
    @Test
    public void parsingTest() throws IOException {
        final GroovyNotificationFilterFactory factory = new GroovyNotificationFilterFactory(getClass().getClassLoader());
        final NotificationFilter filter = factory.create("sequenceNumber == 42L");
        final Notification testNotif = new NotificationBuilder()
                .setSource(this)
                .setMessage("Hello, world!")
                .setSequenceNumber(42L)
                .get();
        assertTrue(filter.isNotificationEnabled(testNotif));
    }
}
