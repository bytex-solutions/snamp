package com.bytex.snamp.connector.composite;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.configuration.ManagedResourceInfo;
import com.bytex.snamp.connector.AbstractManagedResourceConnector;
import com.bytex.snamp.connector.ResourceEventListener;
import com.bytex.snamp.connector.health.HealthCheckSupport;
import com.bytex.snamp.connector.health.HealthStatus;
import com.bytex.snamp.connector.metrics.MetricsSupport;

import javax.annotation.Nonnull;
import java.net.URL;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * Represents resource connector that can combine many resource connectors.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class CompositeResourceConnector extends AbstractManagedResourceConnector implements HealthCheckSupport{
    private final Composition connectors;
    @Aggregation(cached = true)
    private final AttributeComposition attributes;
    @Aggregation(cached = true)
    private final NotificationComposition notifications;
    @Aggregation(cached = true)
    private final OperationComposition operations;

    private CompositeResourceConnector(final String resourceName,
                               final ExecutorService threadPool,
                               final Duration syncPeriod,
                               final URL[] groovyPath) {
        connectors = new Composition(resourceName);
        final ScriptLoader scriptLoader = new ScriptLoader(getClass().getClassLoader(), groovyPath);
        attributes = new AttributeComposition(resourceName, connectors, threadPool, syncPeriod, scriptLoader);
        notifications = new NotificationComposition(resourceName, connectors, threadPool);
        notifications.addNotificationListener(attributes, null, null);
        notifications.setSource(this);
        operations = new OperationComposition(resourceName, connectors);
    }

    CompositeResourceConnector(final String resourceName,
                               final ManagedResourceInfo configuration,
                               final CompositeResourceConfigurationDescriptor parser) {
        this(resourceName, parser.parseThreadPool(configuration),
                parser.parseSyncPeriod(configuration),
                parser.parseGroovyPath(configuration));
        setConfiguration(configuration);
    }

    @Override
    public void addResourceEventListener(final ResourceEventListener listener) {
        addResourceEventListener(listener, attributes, notifications, operations);
    }

    @Override
    public void removeResourceEventListener(final ResourceEventListener listener) {
        removeResourceEventListener(listener, attributes, notifications, operations);
    }

    private void update(final Map<String, ManagedResourceInfo> resources) throws Exception {
        //update supplied connectors
        Acceptor.forEachAccept(resources.entrySet(), entry -> connectors.updateConnector(entry.getKey(), entry.getValue()));
        //dispose connectors that are not specified in the connection string
        connectors.retainConnectors(resources.keySet());
    }

    @Override
    public void update(final ManagedResourceInfo configuration) throws Exception {
        setConfiguration(configuration);
        final CompositeResourceConfigurationDescriptor parser = CompositeResourceConfigurationDescriptor.getInstance();
        final ComposedConfiguration parsedParams = ComposedConfiguration.parse(configuration, parser.parseSeparator(configuration));
        //do update
        update(parsedParams);
    }

    /**
     * Determines whether the connected managed resource is alive.
     *
     * @return Status of the remove managed resource.
     */
    @Override
    @Nonnull
    public HealthStatus getStatus() {
        return connectors.getStatus();
    }

    @Override
    protected MetricsSupport createMetricsReader() {
        return assembleMetricsReader(attributes, operations, notifications);
    }

    /**
     * Releases all resources associated with this connector.
     *
     * @throws Exception Unable to release resources associated with this connector.
     */
    @Override
    public void close() throws Exception {
        connectors.close();
        attributes.close();
        notifications.close();
        operations.close();
        super.close();
    }
}
