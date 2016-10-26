package com.bytex.snamp.connector.composite;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.concurrent.ThreadSafeObject;
import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.attributes.AttributeSupport;
import com.bytex.snamp.connector.notifications.NotificationSupport;
import com.bytex.snamp.connector.operations.OperationSupport;
import com.bytex.snamp.internal.Utils;
import com.google.common.collect.Sets;
import org.osgi.framework.BundleContext;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.bytex.snamp.connector.ManagedResourceConnectorFactoryService.instantiationParameters;

/**
 * Represents composition of managed resource connectors.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class Composition extends ThreadSafeObject implements AttributeSupportProvider, NotificationSupportProvider, OperationSupportProvider, AutoCloseable {
    private final Map<String, ManagedResourceConnector> connectors;
    private final String resourceName;

    Composition(final String resourceName){
        super(SingleResourceGroup.class);
        connectors = new HashMap<>();
        this.resourceName = Objects.requireNonNull(resourceName);
    }

    private static void updateConnector(final Map<String, ManagedResourceConnector> connectors,
                                   final String connectorType,
                                   final String connectionString,
                                   final String resourceName,
                                   final Map<String, String> parameters,
                                        final BundleContext context) throws Exception {
        ManagedResourceConnector connector = connectors.get(connectorType);
        if (connector != null) {  //update existing connector
            try {
                connector.update(connectionString, parameters);
                return;
            } catch (final ManagedResourceConnector.UnsupportedUpdateOperationException e) {
                //connector cannot be updated. We dispose this connector and create a new one
                connector = connectors.remove(connectorType);
                connector.close();
            }
        }
        //create new connector
        connector = ManagedResourceConnectorClient.createConnector(context,
                connectorType,
                instantiationParameters(connectionString, resourceName, parameters)
        );
        connectors.put(connectorType, connector);
    }

    /**
     * Update connector participated in the composition or create a new one.
     * @param connectorType Type of connector to create.
     * @param connectionString Resource connection string.
     * @param parameters Connection parameters.
     * @throws Exception Unable to update connector.
     */
    void updateConnector(final String connectorType, final String connectionString, final Map<String, String> parameters) throws Exception {
        final String resourceName = this.resourceName;
        final BundleContext context = Utils.getBundleContextOfObject(this);
        writeLock.accept(SingleResourceGroup.INSTANCE, connectors, connectors -> updateConnector(connectors, connectorType, connectionString, resourceName, parameters, context), (Duration) null);
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

    private <T> T queryObject(final String connectorType, final Class<T> objectType){
        final ManagedResourceConnector connector = readLock.apply(SingleResourceGroup.INSTANCE, connectors, connectorType, Map::get);
        return connector != null ? connector.queryObject(objectType) : null;
    }

    @Override
    public AttributeSupport getAttributeSupport(final String connectorType) {
        return queryObject(connectorType, AttributeSupport.class);
    }

    @Override
    public NotificationSupport getNotificationSupport(final String connectorType) {
        return queryObject(connectorType, NotificationSupport.class);
    }

    @Override
    public OperationSupport getOperationSupport(final String connectorType) {
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
