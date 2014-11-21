package com.itworks.snamp.connectors.jmx;

import javax.management.*;
import javax.management.remote.JMXConnector;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.*;
import static com.itworks.snamp.configuration.InMemoryAgentConfiguration.InMemoryManagedResourceConfiguration.InMemoryAttributeConfiguration;
import static com.itworks.snamp.configuration.InMemoryAgentConfiguration.InMemoryManagedResourceConfiguration.InMemoryEventConfiguration;
import static com.itworks.snamp.connectors.jmx.JmxConnectorConfigurationDescriptor.OBJECT_NAME_PROPERTY;

/**
 * Represents discovery service provider.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class JmxDiscoveryService{
    private JmxDiscoveryService() {

    }

    private static Collection<AttributeConfiguration> discoverAttributes(final MBeanServerConnection connection) throws IOException, JMException {
        final List<AttributeConfiguration> result = new ArrayList<>(40);
        for (final ObjectName objectName : connection.queryNames(null, null))
            for (final MBeanAttributeInfo attr : connection.getMBeanInfo(objectName).getAttributes()) {
                final InMemoryAttributeConfiguration config = new InMemoryAttributeConfiguration();
                config.getParameters().put(OBJECT_NAME_PROPERTY, objectName.toString());
                config.setAttributeName(attr.getName());
                config.getParameters().put("description", attr.getDescription());
                result.add(config);
            }
        return result;
    }

    private static Collection<EventConfiguration> discoverEvents(final MBeanServerConnection connection) throws IOException, JMException {
        final List<EventConfiguration> result = new ArrayList<>(10);
        for (final ObjectName objectName : connection.queryNames(null, null))
            for (final MBeanNotificationInfo notif : connection.getMBeanInfo(objectName).getNotifications())
                for (final String category : notif.getNotifTypes()) {
                    final InMemoryEventConfiguration config = new InMemoryEventConfiguration();
                    config.setCategory(category);
                    config.getParameters().put(OBJECT_NAME_PROPERTY, objectName.toString());
                    config.getParameters().put("description", notif.getDescription());
                    result.add(config);
                }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static <T extends ManagedEntity> Collection<T> discover(final MBeanServerConnection connection, final Class<T> entityType) throws IOException, JMException {
        if (entityType == null) return Collections.emptyList();
        else if (AttributeConfiguration.class.equals(entityType))
            return (Collection<T>) discoverAttributes(connection);
        else if (EventConfiguration.class.equals(entityType))
            return (Collection<T>) discoverEvents(connection);
        else return Collections.emptyList();
    }

    static <T extends ManagedEntity> Collection<T> discover(final JMXConnector options, final Class<T> entityType) throws IOException, JMException {
        return discover(options.getMBeanServerConnection(), entityType);
    }
}
