package com.bytex.snamp.jmx;

import static com.bytex.snamp.configuration.ManagedResourceConfiguration.EventConfiguration;
import static com.bytex.snamp.configuration.impl.SerializableAgentConfiguration.newEntityConfiguration;
import com.bytex.snamp.connectors.notifications.NotificationDescriptor;
import com.bytex.snamp.connectors.notifications.Severity;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.InvalidSyntaxException;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class ExpressionBasedDescriptorFilterTest extends Assert {
    @Test
    public void notificationFilter() throws InvalidSyntaxException {
        final EventConfiguration config = newEntityConfiguration(EventConfiguration.class);
        assertNotNull(config);
        config.setAlternativeName("cat");
        config.getParameters().put(NotificationDescriptor.SEVERITY_PARAM, Severity.CRITICAL.toString());
        config.getParameters().put("param", "1");
        config.getParameters().put("param2", "2");
        final NotificationDescriptor descriptor = new NotificationDescriptor(config);
        final ExpressionBasedDescriptorFilter filter = new ExpressionBasedDescriptorFilter("(&(severity=critical)(param=1))");
        assertTrue(filter.match(() -> descriptor));
    }
}
