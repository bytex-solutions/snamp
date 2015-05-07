package com.itworks.snamp.jmx;

import com.itworks.snamp.configuration.SerializableAgentConfiguration.SerializableManagedResourceConfiguration.SerializableEventConfiguration;
import com.itworks.snamp.connectors.notifications.NotificationDescriptor;
import com.itworks.snamp.connectors.notifications.NotificationDescriptorRead;
import com.itworks.snamp.connectors.notifications.NotificationSubscriptionModel;
import com.itworks.snamp.connectors.notifications.Severity;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.InvalidSyntaxException;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class ExpressionBasedDescriptorFilterTest extends Assert {
    @Test
    public void notificationFilter() throws InvalidSyntaxException {
        final SerializableEventConfiguration config = new SerializableEventConfiguration();
        config.setCategory("cat");
        config.getParameters().put(NotificationDescriptor.SEVERITY_PARAM, Severity.CRITICAL.toString());
        config.getParameters().put("param", "1");
        config.getParameters().put("param2", "2");
        final NotificationDescriptor descriptor = new NotificationDescriptor(config,
                NotificationSubscriptionModel.UNICAST);
        final ExpressionBasedDescriptorFilter filter = new ExpressionBasedDescriptorFilter("(&(severity=critical)(param=1))");
        assertTrue(filter.match(new NotificationDescriptorRead() {
            @Override
            public NotificationDescriptor getDescriptor() {
                return descriptor;
            }
        }));
    }
}
