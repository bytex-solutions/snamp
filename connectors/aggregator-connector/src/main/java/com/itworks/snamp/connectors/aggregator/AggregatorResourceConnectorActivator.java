package com.itworks.snamp.connectors.aggregator;

import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.connectors.ManagedResourceActivator;

import javax.management.openmbean.CompositeData;
import java.beans.IntrospectionException;
import java.util.Map;
import java.util.Set;

/**
 * Represents activator of {@link AggregatorResourceConnector} resource connector.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class AggregatorResourceConnectorActivator extends ManagedResourceActivator<AggregatorResourceConnector> {
    private static final class ConnectorController extends ManagedResourceConnectorModeler<AggregatorResourceConnector>{

        @Override
        protected boolean addAttribute(final AggregatorResourceConnector connector,
                                    final String attributeID,
                                    final String attributeName,
                                    final TimeSpan readWriteTimeout,
                                    final CompositeData options) {
            return connector.addAttribute(attributeID, attributeName, readWriteTimeout, options);
        }

        @Override
        protected boolean enableNotifications(final AggregatorResourceConnector connector,
                                           final String listId,
                                           final String category,
                                           final CompositeData options) {
            return connector.enableNotifications(listId, category, options);
        }

        @Override
        protected boolean enableOperation(final AggregatorResourceConnector connector, final String operationID, final String operationName, final TimeSpan timeout, final CompositeData options) {
            //not supported
            return false;
        }

        @Override
        protected void removeAttributesExcept(final AggregatorResourceConnector connector,
                                              final Set<String> attributes) {
            connector.removeAttributesExcept(attributes);
        }

        @Override
        protected void disableNotificationsExcept(final AggregatorResourceConnector connector,
                                                  final Set<String> events) {
            connector.disableNotificationsExcept(events);
        }

        @Override
        protected void disableOperationsExcept(final AggregatorResourceConnector connector,
                                               final Set<String> operations) {
            //not supported
        }

        @Override
        public AggregatorResourceConnector createConnector(final String resourceName,
                                                           final String connectionString,
                                                           final Map<String, String> connectionParameters,
                                                           final RequiredService<?>... dependencies) throws IntrospectionException {
            return new AggregatorResourceConnector(resourceName,
                    AggregatorConnectorConfiguration.getNotificationFrequency(connectionParameters));
        }
    }

    private static final class ConfigurationProvider extends ConfigurationEntityDescriptionManager<AggregatorConnectorConfiguration>{
        @Override
        protected AggregatorConnectorConfiguration createConfigurationDescriptionProvider(final RequiredService<?>... dependencies) {
            return new AggregatorConnectorConfiguration();
        }
    }

    private static final class DiscoveryServiceManagerImpl extends DiscoveryServiceManager<AggregatorDiscoveryService>{

        @Override
        protected AggregatorDiscoveryService createDiscoveryService(final RequiredService<?>... dependencies) {
            return new AggregatorDiscoveryService();
        }
    }

    public AggregatorResourceConnectorActivator() {
        super(new ConnectorController(),
                new DiscoveryServiceManagerImpl(),
                new ConfigurationProvider());
    }
}
