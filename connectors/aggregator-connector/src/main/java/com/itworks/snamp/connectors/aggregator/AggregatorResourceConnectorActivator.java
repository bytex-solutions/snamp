package com.itworks.snamp.connectors.aggregator;

import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.connectors.ManagedResourceActivator;

import javax.management.openmbean.CompositeData;
import java.beans.IntrospectionException;
import java.util.Map;

/**
 * Represents activator of {@link AggregatorResourceConnector} resource connector.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class AggregatorResourceConnectorActivator extends ManagedResourceActivator<AggregatorResourceConnector> {
    private static final String NAME = AggregatorResourceConnector.NAME;

    private static final class ConnectorController extends ManagedResourceConnectorModeler<AggregatorResourceConnector>{

        @Override
        protected void addAttribute(final AggregatorResourceConnector connector,
                                    final String attributeID,
                                    final String attributeName,
                                    final TimeSpan readWriteTimeout,
                                    final CompositeData options) {
            connector.addAttribute(attributeID, attributeName, readWriteTimeout, options);
        }

        @Override
        protected void enableNotifications(final AggregatorResourceConnector connector,
                                           final String listId,
                                           final String category,
                                           final CompositeData options) {
            //not supported
        }

        @Override
        protected void addOperation(final AggregatorResourceConnector connector, final String operationID, final String operationName, final CompositeData options) {
            //not supported
        }

        @Override
        public AggregatorResourceConnector createConnector(final String resourceName,
                                                           final String connectionString,
                                                           final Map<String, String> connectionParameters,
                                                           final RequiredService<?>... dependencies) throws IntrospectionException {
            return new AggregatorResourceConnector(resourceName);
        }
    }

    private static final class ConfigurationProvider extends ConfigurationEntityDescriptionManager<AggregatorConnectorConfigurationDescriptor>{
        @Override
        protected AggregatorConnectorConfigurationDescriptor createConfigurationDescriptionProvider(final RequiredService<?>... dependencies) {
            return new AggregatorConnectorConfigurationDescriptor();
        }
    }

    public AggregatorResourceConnectorActivator(){
        super(NAME, new ConnectorController(), new ConfigurationProvider());
    }
}
