package com.bytex.snamp.jmx;

import com.bytex.snamp.configuration.EventConfiguration;
import com.bytex.snamp.connector.notifications.NotificationDescriptor;
import com.bytex.snamp.connector.notifications.Severity;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.InvalidSyntaxException;

import static com.bytex.snamp.configuration.impl.SerializableAgentConfiguration.newEntityConfiguration;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public final class ExpressionBasedDescriptorFilterTest extends Assert {

    @Test
    public void notificationFilter() throws InvalidSyntaxException {
        final EventConfiguration config = newEntityConfiguration(EventConfiguration.class);
        assertNotNull(config);
        config.setAlternativeName("cat");
        config.put(NotificationDescriptor.SEVERITY_PARAM, Severity.CRITICAL.toString());
        config.put("param", "1");
        config.put("param2", "2");
        final NotificationDescriptor descriptor = new NotificationDescriptor(config);
        final ExpressionBasedDescriptorFilter filter = new ExpressionBasedDescriptorFilter("(&(severity=critical)(param=1))");
        assertTrue(filter.match(() -> descriptor));
    }
}
