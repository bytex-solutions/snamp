package com.bytex.snamp.connector.aggregator;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.connector.ManagedResourceActivator;

import java.util.Map;

/**
 * Represents activator of {@link AggregatorResourceConnector} resource connector.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class AggregatorResourceConnectorActivator extends ManagedResourceActivator<AggregatorResourceConnector> {

    @SpecialUse
    public AggregatorResourceConnectorActivator() {
        super(AggregatorResourceConnectorActivator::newResourceConnector,
                discoveryService(AggregatorResourceConnectorActivator::newDiscoveryService),
                configurationDescriptor(AggregatorConnectorConfiguration::new));
    }

    private static AggregatorResourceConnector newResourceConnector(final String resourceName,
                                                                    final String connectionString,
                                                                    final Map<String, String> connectionParameters,
                                                                    final RequiredService<?>... dependencies) {
        return new AggregatorResourceConnector(resourceName,
                AggregatorConnectorConfiguration.getNotificationFrequency(connectionParameters));
    }

    private static AggregatorDiscoveryService newDiscoveryService(final RequiredService<?>... dependencies){
        return new AggregatorDiscoveryService();
    }
}
