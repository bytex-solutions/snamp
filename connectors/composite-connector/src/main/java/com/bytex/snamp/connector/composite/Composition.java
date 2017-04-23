package com.bytex.snamp.connector.composite;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.concurrent.ThreadSafeObject;
import com.bytex.snamp.configuration.ManagedResourceInfo;
import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.attributes.AttributeSupport;
import com.bytex.snamp.connector.health.HealthCheckSupport;
import com.bytex.snamp.connector.health.HealthStatus;
import com.bytex.snamp.connector.health.OkStatus;
import com.bytex.snamp.connector.notifications.NotificationSupport;
import com.bytex.snamp.connector.operations.OperationSupport;
import com.bytex.snamp.internal.Utils;
import com.google.common.collect.Sets;
import org.osgi.framework.BundleContext;

import java.time.Duration;
import java.util.*;

/**
 * Represents composition of managed resource connectors.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class Composition extends ThreadSafeObject implements AttributeSupportProvider, NotificationSupportProvider, OperationSupportProvider, HealthCheckSupport, AutoCloseable {
    private final Map<String, ManagedResourceConnector> connectors;
    private final String resourceName;

    Composition(final String resourceName){
        super(SingleResourceGroup.class);
        connectors = new HashMap<>();
        this.resourceName = Objects.requireNonNull(resourceName);
    }

    private static void updateConnector(final Map<String, ManagedResourceConnector> connectors,
                                        final String connectorType,
                                        final String resourceName,
                                        final ManagedResourceInfo configuration,
                                        final BundleContext context) throws Exception {
        ManagedResourceConnector connector = connectors.get(connectorType);
        if (connector != null) {  //update existing connector
            try {
                connector.update(configuration);
                return;
            } catch (final ManagedResourceConnector.UnsupportedUpdateOperationException e) {
                //connector cannot be updated. We dispose this connector and create a new one
                connector = connectors.remove(connectorType);
                connector.close();
            }
        }
        //create new connector
        connector = ManagedResourceConnectorClient.createConnector(context, connectorType, resourceName, configuration);
        connectors.put(connectorType, connector);
    }

    void updateConnector(final String connectorType, final ManagedResourceInfo configuration) throws Exception {
        final String resourceName = this.resourceName;
        final BundleContext context = Utils.getBundleContextOfObject(this);
        writeLock.accept(SingleResourceGroup.INSTANCE, connectors, connectors -> updateConnector(connectors, connectorType, resourceName, configuration, context), (Duration) null);
    }

    private static void retainConnectors(final Map<String, ManagedResourceConnector> connectors, final Set<String> set) throws Exception {
        Acceptor.forEachAccept(Sets.difference(connectors.keySet(), set), connectorTypeToDispose -> {
            final ManagedResourceConnector connector = connectors.remove(connectorTypeToDispose);
            if (connector != null)
                connector.close();
        });
    }

    /**
     * Disposes all connectors except the specified set.
     * @param set A set of connectors that should stay in this composition.
     * @throws Exception Unable to dispose one or more connectors.
     */
    void retainConnectors(final Set<String> set) throws Exception {
        writeLock.accept(SingleResourceGroup.INSTANCE, connectors, connectors -> retainConnectors(connectors, set), (Duration) null);
    }

    private <T> Optional<T> queryObject(final String connectorType, final Class<T> objectType) {
        return Optional.ofNullable(readLock.apply(SingleResourceGroup.INSTANCE, connectors, connectorType, Map::get))
                .flatMap(connector -> connector.queryObject(objectType));
    }

    private static HealthStatus getStatus(final Iterable<ManagedResourceConnector> connectors) {
        HealthStatus status = OkStatus.getInstance();
        for (final ManagedResourceConnector connector : connectors)
            status = status.worst(connector.queryObject(HealthCheckSupport.class)
                    .map(HealthCheckSupport::getStatus)
                    .orElseGet(OkStatus::getInstance)
            );
        return status;
    }

    /**
     * Determines whether the connected managed resource is alive.
     *
     * @return Status of the remove managed resource.
     */
    @Override
    public HealthStatus getStatus() {
        return readLock.apply(SingleResourceGroup.INSTANCE, connectors, connectors -> getStatus(connectors.values()));
    }

    @Override
    public Optional<AttributeSupport> getAttributeSupport(final String connectorType) {
        return queryObject(connectorType, AttributeSupport.class);
    }

    @Override
    public Optional<NotificationSupport> getNotificationSupport(final String connectorType) {
        return queryObject(connectorType, NotificationSupport.class);
    }

    @Override
    public Optional<OperationSupport> getOperationSupport(final String connectorType) {
        return queryObject(connectorType, OperationSupport.class);
    }

    private static void releaseConnectors(final Map<String, ManagedResourceConnector> connectors) throws Exception{
        Acceptor.forEachAccept(connectors.values(), ManagedResourceConnector::close);
        connectors.clear();
    }

    /**
     * Disposes all connectors inside of this composition.
     * @throws Exception Unable to dispose one or more connectors.
     */
    @Override
    public void close() throws Exception {
        writeLock.accept(SingleResourceGroup.INSTANCE, connectors, Composition::releaseConnectors, (Duration) null);
    }
}
