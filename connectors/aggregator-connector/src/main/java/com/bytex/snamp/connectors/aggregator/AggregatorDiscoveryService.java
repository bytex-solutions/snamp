package com.bytex.snamp.connectors.aggregator;

import com.bytex.snamp.internal.Utils;
import com.google.common.collect.ImmutableList;
import com.bytex.snamp.AbstractAggregator;
import com.bytex.snamp.configuration.AbstractAgentConfiguration;
import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.connectors.discovery.DiscoveryResultBuilder;
import com.bytex.snamp.connectors.discovery.DiscoveryService;
import org.osgi.framework.BundleContext;

import java.util.*;
import java.util.logging.Logger;

import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.*;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
final class AggregatorDiscoveryService extends AbstractAggregator implements DiscoveryService {

    private static Collection<AttributeConfiguration> discoverAttributes(final BundleContext context){
        return ImmutableList.of(
            PatternMatcher.getConfiguration(context),
                UnaryComparison.getConfiguration(context),
                BinaryComparison.getConfiguration(context),
                BinaryPercent.getConfiguration(context),
                UnaryPercent.getConfiguration(context),
                Counter.getConfiguration(context),
                Average.getConfiguration(context),
                Peak.getConfiguration(context),
                Decomposer.getConfiguration(context),
                Stringifier.getConfiguration(context),
                Composer.getConfiguration(context)
        );
    }

    private static Collection<EventConfiguration> discoverEvents(final BundleContext context){
        return ImmutableList.of(
            PeriodicAttributeQuery.getConfiguration(context),
            HealthCheckNotification.getConfiguration(context)
        );
    }

    private BundleContext getBundleContext(){
        return Utils.getBundleContextOfObject(this);
    }

    /**
     * Attempts to discover collection of managed entities (such as attributes or notifications)
     * using managed resource connection string.
     * <p/>
     * Do not add elements from the returned collection directly in {@link AgentConfiguration.ManagedResourceConfiguration#getFeatures(Class)}
     * result set, use the following algorithm:
     * <ul>
     * <li>Use {@link AbstractAgentConfiguration#copy(AttributeConfiguration, AttributeConfiguration)}
     * or {@link AbstractAgentConfiguration#copy(EventConfiguration, EventConfiguration)} method
     * to copy managed entity returned by this method into the newly created entity.</li>
     * </ul>
     *
     * @param connectionString  Managed resource connection string.
     * @param connectionOptions Managed resource connection options (see {@link AgentConfiguration.ManagedResourceConfiguration#getConnectionString()}).
     * @param entityType        Type of the managed entity (see {@link AgentConfiguration.ManagedResourceConfiguration#getParameters()}).
     * @return A collection of discovered entities; or empty collection if no entities
     * was detected.
     * @see AttributeConfiguration
     * @see EventConfiguration
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends FeatureConfiguration> Collection<T> discover(final String connectionString, final Map<String, String> connectionOptions, final Class<T> entityType) {
        if(Objects.equals(entityType, AttributeConfiguration.class))
            return (Collection<T>)discoverAttributes(getBundleContext());
        else if(Objects.equals(entityType, EventConfiguration.class))
            return (Collection<T>)discoverEvents(getBundleContext());
        else return Collections.emptyList();
    }

    /**
     * Attempts to discover collection of managed entities in batch manner.
     *
     * @param connectionString  Managed resource connection string.
     * @param connectionOptions Managed resource connection options (see {@link AgentConfiguration.ManagedResourceConfiguration#getConnectionString()}).
     * @param entityTypes       An array of requested entity types.
     * @return Discovery result.
     */
    @SafeVarargs
    @Override
    public final DiscoveryResult discover(final String connectionString, final Map<String, String> connectionOptions, final Class<? extends FeatureConfiguration>... entityTypes) {
        final DiscoveryResultBuilder result = new DiscoveryResultBuilder();
        for(final Class<? extends FeatureConfiguration> type: entityTypes)
            result.importFeatures(this, connectionString, connectionOptions, type);
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
