package com.bytex.snamp.connectors.jmx;

import com.bytex.snamp.configuration.ConfigurationManager;

import javax.management.*;
import javax.management.remote.JMXConnector;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.bytex.snamp.configuration.ManagedResourceConfiguration.*;
import static com.bytex.snamp.connectors.jmx.JmxConnectorDescriptionProvider.OBJECT_NAME_PROPERTY;

/**
 * Represents discovery service provider.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
final class JmxDiscoveryService{
    private JmxDiscoveryService() {
        throw new InstantiationError();
    }

    private static Collection<AttributeConfiguration> discoverAttributes(final ClassLoader context, final MBeanServerConnection connection) throws IOException, JMException {
        final List<AttributeConfiguration> result = new ArrayList<>(40);
        for (final ObjectName objectName : connection.queryNames(null, null))
            for (final MBeanAttributeInfo attr : connection.getMBeanInfo(objectName).getAttributes()) {
                final AttributeConfiguration config = ConfigurationManager.createEntityConfiguration(context, AttributeConfiguration.class);
                if(config != null) {
                    config.getParameters().put(OBJECT_NAME_PROPERTY, objectName.getCanonicalName());
                    config.setAlternativeName(attr.getName());
                    config.getParameters().put("description", attr.getDescription());
                    result.add(config);
                }
            }
        return result;
    }

    private static Collection<EventConfiguration> discoverEvents(final ClassLoader context, final MBeanServerConnection connection) throws IOException, JMException {
        final List<EventConfiguration> result = new ArrayList<>(10);
        for (final ObjectName objectName : connection.queryNames(null, null))
            for (final MBeanNotificationInfo notif : connection.getMBeanInfo(objectName).getNotifications())
                for (final String category : notif.getNotifTypes()) {
                    final EventConfiguration config = ConfigurationManager.createEntityConfiguration(context, EventConfiguration.class);
                    if(config != null) {
                        config.setAlternativeName(category);
                        config.getParameters().put(OBJECT_NAME_PROPERTY, objectName.getCanonicalName());
                        config.getParameters().put("description", notif.getDescription());
                        result.add(config);
                    }
                }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static <T extends FeatureConfiguration> Collection<T> discover(final ClassLoader context, final MBeanServerConnection connection, final Class<T> entityType) throws IOException, JMException {
        if (entityType == null) return Collections.emptyList();
        else if (AttributeConfiguration.class.equals(entityType))
            return (Collection<T>) discoverAttributes(context, connection);
        else if (EventConfiguration.class.equals(entityType))
            return (Collection<T>) discoverEvents(context, connection);
        else return Collections.emptyList();
    }

    static <T extends FeatureConfiguration> Collection<T> discover(final ClassLoader context, final JMXConnector options, final Class<T> entityType) throws IOException, JMException {
        return discover(context, options.getMBeanServerConnection(), entityType);
    }
}
