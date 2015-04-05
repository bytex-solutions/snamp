package com.itworks.snamp.connectors.discovery;

import com.itworks.snamp.AbstractAggregator;
import com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.FeatureConfiguration;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractDiscoveryService<TProvider extends AutoCloseable> extends AbstractAggregator implements DiscoveryService {
    private static final class DiscoverResultImpl extends HashMap<Class<? extends FeatureConfiguration>, Collection<? extends FeatureConfiguration>> implements DiscoveryResult {
        private DiscoverResultImpl(final int capacity) {
            super(capacity);
        }

        /**
         * Retrieves a sub-result of discovery operation.
         *
         * @param entityType Type of the discovered managed entity.
         * @return A collection of discovered entities.
         * @throws IllegalArgumentException The specified managed entity was not requested.
         */
        @SuppressWarnings("unchecked")
        @Override
        public <T extends FeatureConfiguration> Collection<T> getSubResult(final Class<T> entityType) throws IllegalArgumentException {
            if (containsKey(entityType))
                return (Collection<T>) get(entityType);
            else throw new IllegalArgumentException(String.format("Entity type %s was not requested", entityType));
        }
    }

    /**
     * Initializes a new discovery service.
     */
    protected AbstractDiscoveryService(){

    }


    /**
     * Attempts to discover collection of managed entities (such as attributes or notifications)
     * using managed resource connection string.
     * <p/>
     * Do not add elements from the returned collection directly in {@link com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration#getElements(Class)}
     * result set, use the following algorithm:
     * <ul>
     * <li>Create a new managed entity with {@link com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration#newElement(Class)} method.</li>
     * <li>Use {@link com.itworks.snamp.configuration.AbstractAgentConfiguration#copy(com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration, com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration)}
     * or {@link com.itworks.snamp.configuration.AbstractAgentConfiguration#copy(com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration, com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration)} method
     * to copy managed entity returned by this method into the newly created entity.</li>
     * </ul>
     *
     * @param connectionString  Managed resource connection string.
     * @param connectionOptions Managed resource connection options (see {@link com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration#getConnectionString()}).
     * @param entityType        Type of the managed entity (see {@link com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration#getParameters()}).
     * @return A collection of discovered entities; or empty collection if no entities
     * was detected.
     * @see com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration
     * @see com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration
     */
    @Override
    public final <T extends FeatureConfiguration> Collection<T> discover(final String connectionString, final Map<String, String> connectionOptions, final Class<T> entityType) {
        try(final TProvider provider = createProvider(connectionString, connectionOptions)){
            return getEntities(entityType, provider);
        }
        catch (final Exception e){
            discoveryFailed(e);
            return Collections.emptyList();
        }
    }

    /**
     * Creates management information provider.
     * @param connectionString Managed resource connection string.
     * @param connectionOptions Managed resource connection options (see {@link com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration#getParameters()}).
     * @return A new instance of the management information provider.
     * @throws java.lang.Exception Unable to instantiate provider.
     */
    protected abstract TProvider createProvider(final String connectionString,
                                                final Map<String, String> connectionOptions) throws Exception;

    /**
     * Extracts management information from provider.
     * @param entityType Type of the requested information.
     * @param provider Management information provider.
     * @param <T> Type of the requested management information.
     * @return An extracted management information; or empty collection if no information provided.
     * @throws java.lang.Exception Unable to extract information.
     */
    protected abstract <T extends FeatureConfiguration> Collection<T> getEntities(final Class<T> entityType, final TProvider provider) throws Exception;

    /**
     * Attempts to discover collection of managed entities in batch manner.
     *
     * @param connectionString  Managed resource connection string.
     * @param connectionOptions Managed resource connection options (see {@link com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration#getConnectionString()}).
     * @param entityTypes       An array of requested entity types.
     * @return Discovery result.
     */
    @SafeVarargs
    @Override
    public final DiscoveryResult discover(final String connectionString, final Map<String, String> connectionOptions, final Class<? extends FeatureConfiguration>... entityTypes) {
        try (final TProvider provider = createProvider(connectionString, connectionOptions)) {
            final DiscoverResultImpl result = new DiscoverResultImpl(entityTypes.length);
            for (final Class<? extends FeatureConfiguration> t : entityTypes)
                result.put(t, getEntities(t, provider));
            return result;

        } catch (final Exception e) {
            discoveryFailed(e);
            return new EmptyDiscoveryResult(entityTypes);
        }
    }

    private void discoveryFailed(final Exception e){
        getLogger().log(Level.WARNING, "Discovery request failed.", e);
    }
}
