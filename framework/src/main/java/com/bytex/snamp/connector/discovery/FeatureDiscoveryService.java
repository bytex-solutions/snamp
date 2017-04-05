package com.bytex.snamp.connector.discovery;

import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.configuration.EventConfiguration;
import com.bytex.snamp.configuration.FeatureConfiguration;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import com.bytex.snamp.core.SupportService;
import com.google.common.collect.ImmutableSet;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Additional service that can be exposed by {@link com.bytex.snamp.connector.ManagedResourceActivator}
 * class that provides discovery method for management attributes or notifications.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public interface FeatureDiscoveryService extends SupportService {
    /**
     * Represents result of batch discovery operation.
     * @author Roman Sakno
     * @since 1.0
     * @version 2.0
     */
    interface DiscoveryResult{
        /**
         * Retrieves a sub-result of discovery operation.
         * @param entityType Type of the discovered managed entity.
         * @param <T> Type of the requested managed entity.
         * @return A collection of discovered entities.
         * @throws java.lang.IllegalArgumentException The specified managed entity was not requested.
         */
        <T extends FeatureConfiguration> Collection<T> getSubResult(final Class<T> entityType) throws IllegalArgumentException;
    }



    /**
     * Represents an empty discovery result.
     * @author Roman Sakno
     * @since 1.0
     * @version 2.0
     */
    final class EmptyDiscoveryResult implements DiscoveryResult{
        private final Set<Class<? extends FeatureConfiguration>> entities;

        /**
         * Initializes a new empty discovery result.
         * @param requestedEntities An array of requested entities.
         */
        @SafeVarargs
        public EmptyDiscoveryResult(final Class<? extends FeatureConfiguration>... requestedEntities){
            entities = ImmutableSet.copyOf(requestedEntities);
        }

        /**
         * Retrieves a sub-result of discovery operation.
         *
         * @param entityType Type of the discovered managed entity.
         * @return A collection of discovered entities.
         * @throws IllegalArgumentException The specified managed entity was not requested.
         */
        @Override
        public <T extends FeatureConfiguration> Collection<T> getSubResult(final Class<T> entityType) throws IllegalArgumentException {
            if(entities.contains(entityType)) return Collections.emptyList();
            else throw new IllegalArgumentException(String.format("Entity type %s was not requested", entityType));
        }
    }

    /**
     * Attempts to discover collection of managed entities (such as attributes or notifications)
     * using managed resource connection string.
     * @param connectionString Managed resource connection string.
     * @param connectionOptions Managed resource connection options (see {@link ManagedResourceConfiguration#getConnectionString()}).
     * @param entityType Type of the managed entity.
     * @param <T> Type of the managed entity.
     * @return A collection of discovered entities; or empty collection if no entities
     * was detected.
     * @see AttributeConfiguration
     * @see EventConfiguration
     */
    <T extends FeatureConfiguration> Collection<T> discover(final String connectionString,
                                                            final Map<String, String> connectionOptions,
                                                            final Class<T> entityType);

    /**
     * Attempts to discover collection of managed entities in batch manner.
     * @param connectionString Managed resource connection string.
     * @param connectionOptions Managed resource connection options (see {@link ManagedResourceConfiguration#getConnectionString()}).
     * @param entityTypes An array of requested entity types.
     * @return Discovery result.
     */
    @SuppressWarnings("unchecked")
    DiscoveryResult discover(final String connectionString,
                             final Map<String, String> connectionOptions,
                             final Class<? extends FeatureConfiguration>... entityTypes);
}