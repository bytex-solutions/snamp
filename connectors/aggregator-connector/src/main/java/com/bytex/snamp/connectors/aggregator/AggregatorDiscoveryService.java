package com.bytex.snamp.connectors.aggregator;

import com.bytex.snamp.AbstractAggregator;
import com.bytex.snamp.configuration.AbstractAgentConfiguration;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import com.bytex.snamp.connectors.discovery.DiscoveryResultBuilder;
import com.bytex.snamp.connectors.discovery.DiscoveryService;
import com.google.common.collect.ImmutableList;

import java.util.*;
import java.util.logging.Logger;

import static com.bytex.snamp.configuration.ManagedResourceConfiguration.*;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class AggregatorDiscoveryService extends AbstractAggregator implements DiscoveryService {

    private static Collection<AttributeConfiguration> discoverAttributes(){
        return ImmutableList.of(
            PatternMatcher.getConfiguration(),
                UnaryComparison.getConfiguration(),
                BinaryComparison.getConfiguration(),
                BinaryPercent.getConfiguration(),
                UnaryPercent.getConfiguration(),
                Counter.getConfiguration(),
                Average.getConfiguration(),
                Peak.getConfiguration(),
                Decomposer.getConfiguration(),
                Stringifier.getConfiguration(),
                Composer.getConfiguration()
        );
    }

    private static Collection<EventConfiguration> discoverEvents(){
        return ImmutableList.of(
            PeriodicAttributeQuery.getConfiguration(),
            HealthCheckNotification.getConfiguration()
        );
    }

    /**
     * Attempts to discover collection of managed entities (such as attributes or notifications)
     * using managed resource connection string.
     * <p/>
     * Do not add elements from the returned collection directly in {@link ManagedResourceConfiguration#getFeatures(Class)}
     * result set, use the following algorithm:
     * <ul>
     * <li>Use {@link AbstractAgentConfiguration#copy(AttributeConfiguration, AttributeConfiguration)}
     * or {@link AbstractAgentConfiguration#copy(EventConfiguration, EventConfiguration)} method
     * to copy managed entity returned by this method into the newly created entity.</li>
     * </ul>
     *
     * @param connectionString  Managed resource connection string.
     * @param connectionOptions Managed resource connection options (see {@link ManagedResourceConfiguration#getConnectionString()}).
     * @param entityType        Type of the managed entity (see {@link ManagedResourceConfiguration#getParameters()}).
     * @return A collection of discovered entities; or empty collection if no entities
     * was detected.
     * @see AttributeConfiguration
     * @see EventConfiguration
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends FeatureConfiguration> Collection<T> discover(final String connectionString, final Map<String, String> connectionOptions, final Class<T> entityType) {
        if(Objects.equals(entityType, AttributeConfiguration.class))
            return (Collection<T>)discoverAttributes();
        else if(Objects.equals(entityType, EventConfiguration.class))
            return (Collection<T>)discoverEvents();
        else return Collections.emptyList();
    }

    /**
     * Attempts to discover collection of managed entities in batch manner.
     *
     * @param connectionString  Managed resource connection string.
     * @param connectionOptions Managed resource connection options (see {@link ManagedResourceConfiguration#getConnectionString()}).
     * @param entityTypes       An array of requested entity types.
     * @return Discovery result.
     */
    @SafeVarargs
    @Override
    public final DiscoveryResult discover(final String connectionString, final Map<String, String> connectionOptions, final Class<? extends FeatureConfiguration>... entityTypes) {
        final DiscoveryResultBuilder result = new DiscoveryResultBuilder();
        Arrays.stream(entityTypes).forEach(type -> result.importFeatures(this, connectionString, connectionOptions, type));
        return result.get();
    }

    /**
     * Gets logger associated with this service.
     *
     * @return The logger associated with this service.
     */
    @Override
    public Logger getLogger() {
        return AggregatorResourceConnector.getLoggerImpl();
    }
}
