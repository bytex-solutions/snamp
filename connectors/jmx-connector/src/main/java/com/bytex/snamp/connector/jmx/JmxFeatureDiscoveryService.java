package com.bytex.snamp.connector.jmx;

import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.configuration.EventConfiguration;
import com.bytex.snamp.configuration.FeatureConfiguration;
import com.bytex.snamp.connector.discovery.AbstractFeatureDiscoveryService;

import javax.management.*;
import javax.management.remote.JMXConnector;
import java.io.IOException;
import java.util.*;

import static com.bytex.snamp.connector.jmx.JmxConnectorDescriptionProvider.OBJECT_NAME_PROPERTY;

/**
 * Represents discovery service provider.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class JmxFeatureDiscoveryService extends AbstractFeatureDiscoveryService<JMXConnector> {

    private static Collection<AttributeConfiguration> discoverAttributes(final ClassLoader context, final MBeanServerConnection connection) throws IOException, JMException {
        final List<AttributeConfiguration> result = new ArrayList<>(40);
        for (final ObjectName objectName : connection.queryNames(null, null))
            for (final MBeanAttributeInfo attr : connection.getMBeanInfo(objectName).getAttributes()) {
                final AttributeConfiguration config = ConfigurationManager.createEntityConfiguration(context, AttributeConfiguration.class);
                if(config != null) {
                    config.put(OBJECT_NAME_PROPERTY, objectName.getCanonicalName());
                    config.setAlternativeName(attr.getName());
                    config.setDescription(attr.getDescription());
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
                        config.put(OBJECT_NAME_PROPERTY, objectName.getCanonicalName());
                        config.setDescription(notif.getDescription());
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

    private static <T extends FeatureConfiguration> Collection<T> discover(final ClassLoader context, final JMXConnector options, final Class<T> entityType) throws IOException, JMException {
        return discover(context, options.getMBeanServerConnection(), entityType);
    }

    @Override
    protected JMXConnector createProvider(final String connectionString, final Map<String, String> connectionOptions) throws IOException, MalformedObjectNameException {
        return new JmxConnectionOptions(connectionString, connectionOptions).createConnection();
    }

    @Override
    protected <T extends FeatureConfiguration> Collection<T> getEntities(final Class<T> entityType, final JMXConnector options) throws IOException, JMException {
        return discover(getClass().getClassLoader(), options, entityType);
    }
}
