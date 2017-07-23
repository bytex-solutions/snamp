package com.bytex.snamp.connector.composite;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.concurrent.ConcurrentResourceAccessor;
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

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Represents composition of managed resource connectors.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class Composition extends ConcurrentResourceAccessor<Map<String, ManagedResourceConnector>> implements AttributeSupportProvider, NotificationSupportProvider, OperationSupportProvider, HealthCheckSupport, AutoCloseable {
    private static final long serialVersionUID = 6877959325451075556L;
    private final String resourceName;

    Composition(final String resourceName){
        super(new HashMap<>());
        this.resourceName = Objects.requireNonNull(resourceName);
    }

    private static Void updateConnector(final Map<String, ManagedResourceConnector> connectors,
                                        final String connectorType,
                                        final String resourceName,
                                        final ManagedResourceInfo configuration,
                                        final BundleContext context) throws Exception {
        ManagedResourceConnector connector = connectors.get(connectorType);
        if (connector != null) {  //update existing connector
            try {
                connector.update(configuration);
                return null;
            } catch (final ManagedResourceConnector.UnsupportedUpdateOperationException e) {
                //connector cannot be updated. We dispose this connector and create a new one
                connector = connectors.remove(connectorType);
                connector.close();
            }
        }
        //create new connector
        connector = ManagedResourceConnectorClient.createConnector(context, connectorType, resourceName, configuration);
        connectors.put(connectorType, connector);
        return null;
    }

    void updateConnector(final String connectorType, final ManagedResourceInfo configuration) throws Exception {
        final String resourceName = this.resourceName;
        final BundleContext context = Utils.getBundleContextOfObject(this);
        write(connectors -> updateConnector(connectors, connectorType, resourceName, configuration, context), null);
    }

    private static Void retainConnectors(final Map<String, ManagedResourceConnector> connectors, final Set<String> set) throws Exception {
        Acceptor.forEachAccept(Sets.difference(connectors.keySet(), set), connectorTypeToDispose -> {
            final ManagedResourceConnector connector = connectors.remove(connectorTypeToDispose);
            if (connector != null)
                connector.close();
        });
        return null;
    }

    /**
     * Disposes all connectors except the specified set.
     * @param set A set of connectors that should stay in this composition.
     * @throws Exception Unable to dispose one or more connectors.
     */
    void retainConnectors(final Set<String> set) throws Exception {
        write(connectors -> retainConnectors(connectors, set), null);
    }

    private <T> Optional<T> queryObject(final String connectorType, final Class<T> objectType) {
        return read(connectors -> Optional.ofNullable(connectors.get(connectorType)))
                .flatMap(connector -> connector.queryObject(objectType));
    }

    private static HealthStatus getStatus(final Iterable<ManagedResourceConnector> connectors) {
        HealthStatus status = new OkStatus();
        for (final ManagedResourceConnector connector : connectors)
            status = status.worst(connector.queryObject(HealthCheckSupport.class)
                    .map(HealthCheckSupport::getStatus)
                    .orElseGet(OkStatus::new)
            );
        return status;
    }

    /**
     * Determines whether the connected managed resource is alive.
     *
     * @return Status of the remove managed resource.
     */
    @Override
    @Nonnull
    public HealthStatus getStatus() {
        return read(connectors -> getStatus(connectors.values()));
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

    private static Void releaseConnectors(final Map<String, ManagedResourceConnector> connectors) throws Exception{
        Acceptor.forEachAccept(connectors.values(), ManagedResourceConnector::close);
        connectors.clear();
        return null;
    }

    /**
     * Disposes all connectors inside of this composition.
     * @throws Exception Unable to dispose one or more connectors.
     */
    @Override
    public void close() throws Exception {
        write(Composition::releaseConnectors, null);
    }
}
