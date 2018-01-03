package com.bytex.snamp.connector.composite;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.concurrent.ConcurrentResourceAccessor;
import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeManager;
import com.bytex.snamp.connector.attributes.AttributeRepository;
import com.bytex.snamp.connector.health.HealthStatus;
import com.bytex.snamp.connector.health.OkStatus;
import com.bytex.snamp.connector.notifications.NotificationManager;
import com.bytex.snamp.connector.operations.OperationManager;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.jmx.JMExceptionUtils;
import com.google.common.collect.Sets;
import org.osgi.framework.BundleContext;

import javax.annotation.Nonnull;
import javax.management.AttributeNotFoundException;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import java.util.*;

/**
 * Represents composition of managed resource connectors.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
final class Composition extends ConcurrentResourceAccessor<Map<String, ManagedResourceConnector>> implements NotificationSupportProvider, OperationSupportProvider, AutoCloseable {
    @FunctionalInterface
    private interface ManagedResourceConnectorReader<O>{
        O apply(@Nonnull final ManagedResourceConnector input) throws JMException;
    }

    private static final long serialVersionUID = 6877959325451075556L;
    private final String resourceName;

    Composition(final String resourceName){
        super(new HashMap<>());
        this.resourceName = Objects.requireNonNull(resourceName);
    }

    private static Void updateConnector(final Map<String, ManagedResourceConnector> connectors,
                                        final String connectorType,
                                        final String resourceName,
                                        final String connectionString,
                                        final Map<String, String> configuration,
                                        final BundleContext context) throws Exception {
        ManagedResourceConnector connector = connectors.remove(connectorType);
        if (connector != null) {  //update existing connector
            final Optional<ManagedResourceConnector.Updater> updater = connector.queryObject(ManagedResourceConnector.Updater.class);
            if (updater.isPresent())
                try {
                    updater.get().update(connectionString, configuration);
                    return null;
                } catch (final Exception e) {
                    //connector cannot be updated. We dispose this connector and create a new one
                }
            connector.close();
        }
        //create new connector
        connector = ManagedResourceConnectorClient.createConnector(context, connectorType, resourceName, connectionString, configuration);
        connectors.put(connectorType, connector);
        return null;
    }

    void updateConnector(final String connectorType,
                         final String connectionString,
                         final Map<String, String> configuration) throws Exception {
        final String resourceName = this.resourceName;
        final BundleContext context = Utils.getBundleContextOfObject(this);
        write(connectors -> updateConnector(connectors, connectorType, resourceName, connectionString, configuration, context), null);
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

    private <T> T readConnector(final String connectorType, final ManagedResourceConnectorReader<T> reader) throws JMException {
        return read(connectors -> {
            final ManagedResourceConnector connector = connectors.get(connectorType);
            if(connector == null)
                throw new IllegalArgumentException(String.format("Connector %s was not specified in connection string", connectorType));
            else
                return reader.apply(connector);
        });
    }

    private static HealthStatus getStatus(final Collection<ManagedResourceConnector> connectors) {
        return connectors.stream().map(ManagedResourceConnector::getStatus).reduce(new OkStatus(), HealthStatus::worst);
    }

    /**
     * Determines whether the connected managed resource is alive.
     *
     * @return Status of the remove managed resource.
     */
    @Nonnull
    public HealthStatus getSummaryStatus() {
        return read(connectors -> getStatus(connectors.values()));
    }

    MBeanAttributeInfo addAttribute(final String connectorType,
                                    final String attributeName,
                                    final AttributeDescriptor descriptor) throws JMException {
        return readConnector(connectorType, connector -> {
            final AttributeManager manager = connector.queryObject(AttributeManager.class)
                    .orElseThrow(() -> new UnsupportedOperationException("Connector " + connectorType + " doesn't support attributes"));
            manager.addAttribute(attributeName, descriptor);
            return AttributeRepository.findAttribute(attributeName, connector.getMBeanInfo())
                    .orElseThrow(() -> JMExceptionUtils.attributeNotFound(attributeName));
        });
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
