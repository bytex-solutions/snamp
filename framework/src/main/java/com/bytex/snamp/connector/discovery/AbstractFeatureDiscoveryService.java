package com.bytex.snamp.connector.discovery;

import com.bytex.snamp.AbstractAggregator;
import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.configuration.EventConfiguration;
import com.bytex.snamp.configuration.FeatureConfiguration;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import com.bytex.snamp.core.LoggingScope;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public abstract class AbstractFeatureDiscoveryService<TProvider extends AutoCloseable> extends AbstractAggregator implements FeatureDiscoveryService {
    private static final class DiscoveryLoggingScope extends LoggingScope {
        private DiscoveryLoggingScope(final FeatureDiscoveryService service) {
            super(service, "discovery");
        }

        private void discoveryFailed(final Exception e) {
            log(Level.WARNING, "Discovery request failed.", e);
        }
    }

    /**
     * Initializes a new discovery service.
     */
    protected AbstractFeatureDiscoveryService() {

    }


    /**
     * Attempts to discover collection of managed entities (such as attributes or notifications)
     * using managed resource connection string.
     *
     * @param connectionString  Managed resource connection string.
     * @param connectionOptions Managed resource connection options (see {@link ManagedResourceConfiguration#getConnectionString()}).
     * @param entityType        Type of the managed entity (see {@link ManagedResourceConfiguration}).
     * @return A collection of discovered entities; or empty collection if no entities
     * was detected.
     * @see AttributeConfiguration
     * @see EventConfiguration
     */
    @Override
    public final <T extends FeatureConfiguration> Collection<T> discover(final String connectionString, final Map<String, String> connectionOptions, final Class<T> entityType) {
        final DiscoveryLoggingScope discoveryLogging = new DiscoveryLoggingScope(this);
        try (final TProvider provider = createProvider(connectionString, connectionOptions)) {
            return getEntities(entityType, provider);
        } catch (final Exception e) {
            discoveryLogging.discoveryFailed(e);
            return Collections.emptyList();
        }
    }

    /**
     * Creates management information provider.
     *
     * @param connectionString  Managed resource connection string.
     * @param connectionOptions Managed resource connection options (see {@link ManagedResourceConfiguration#getParameters()}).
     * @return A new instance of the management information provider.
     * @throws java.lang.Exception Unable to instantiate provider.
     */
    protected abstract TProvider createProvider(final String connectionString,
                                                final Map<String, String> connectionOptions) throws Exception;

    /**
     * Extracts management information from provider.
     *
     * @param entityType Type of the requested information.
     * @param provider   Management information provider.
     * @param <T>        Type of the requested management information.
     * @return An extracted management information; or empty collection if no information provided.
     * @throws java.lang.Exception Unable to extract information.
     */
    protected abstract <T extends FeatureConfiguration> Collection<T> getEntities(final Class<T> entityType, final TProvider provider) throws Exception;

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
        final DiscoveryLoggingScope discoveryLogging = new DiscoveryLoggingScope(this);
        try (final TProvider provider = createProvider(connectionString, connectionOptions)) {
            final DiscoveryResultBuilder builder = new DiscoveryResultBuilder();
            for (final Class<? extends FeatureConfiguration> t : entityTypes)
                builder.addFeatures(t, getEntities(t, provider));
            return builder.get();
        } catch (final Exception e) {
            discoveryLogging.discoveryFailed(e);
            return new EmptyDiscoveryResult(entityTypes);
        } finally {
            discoveryLogging.close();
        }
    }
}