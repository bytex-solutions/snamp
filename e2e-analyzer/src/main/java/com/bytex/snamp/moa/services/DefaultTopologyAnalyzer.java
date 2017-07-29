package com.bytex.snamp.moa.services;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.notifications.NotificationContainer;
import com.bytex.snamp.connector.notifications.NotificationSupport;
import com.bytex.snamp.core.AbstractFrameworkServiceTracker;
import com.bytex.snamp.core.ServiceSelector;
import com.bytex.snamp.instrumentation.measurements.jmx.SpanNotification;
import com.bytex.snamp.moa.topology.ComponentVertex;
import com.bytex.snamp.moa.topology.TopologyAnalyzer;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;

import javax.annotation.Nonnull;
import javax.annotation.WillNotClose;
import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.Notification;
import javax.management.NotificationListener;
import java.util.logging.Level;

/**
 * Represents implementation of topology analyzer.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
final class DefaultTopologyAnalyzer extends AbstractFrameworkServiceTracker<ManagedResourceConnector, ManagedResourceConnectorClient> implements TopologyAnalyzer, SafeCloseable, NotificationListener {
    private final FilteredGraphOfComponents graph;
    private final Filter resourceFilter;

    DefaultTopologyAnalyzer(final long historySize){
        super(ManagedResourceConnector.class);
        graph = new FilteredGraphOfComponents(historySize);
        final ServiceSelector builder = ManagedResourceConnectorClient.selector();
        builder.addServiceListener(getBundleContext(), this);
        resourceFilter = builder.get();
    }

    /**
     * Resets internal state of the object.
     */
    @Override
    public void reset() {
        graph.reset();
    }

    @Override
    public <E extends Throwable> void visitVertices(final Acceptor<? super ComponentVertex, E> visitor) throws E {
        graph.forEach(visitor);
    }

    /**
     * Returns filter used to query managed resource connectors from OSGi environment.
     *
     * @return A filter used to query managed resource connectors from OSGi environment.
     * @implSpec This operation must be idempotent and return the same filter for every call.
     */
    @Nonnull
    @Override
    protected Filter getServiceFilter() {
        return resourceFilter;
    }

    @Nonnull
    @Override
    protected ManagedResourceConnectorClient createClient(final ServiceReference<ManagedResourceConnector> serviceRef) throws InstanceNotFoundException {
        return new ManagedResourceConnectorClient(getBundleContext(), serviceRef);
    }

    @Override
    protected String getServiceId(final ManagedResourceConnectorClient client) {
        return client.getManagedResourceName();
    }

    @Override
    protected synchronized void addService(@WillNotClose final ManagedResourceConnectorClient connector) {
        final String resourceName = getServiceId(connector);
        if (trackedServices.contains(resourceName)) {
            getLogger().info(String.format("Resource %s is already attached to the topology analyzer", resourceName));
        } else {
            graph.add(connector.getGroupName());
            connector.queryObject(NotificationSupport.class).ifPresent(this::addNotificationListener);
        }
    }

    @Override
    protected synchronized void removeService(@WillNotClose final ManagedResourceConnectorClient connector) {
        final String resourceName = getServiceId(connector);
        if (trackedServices.contains(resourceName)) {
            connector.queryObject(NotificationSupport.class).ifPresent(this::removeNotificationListener);
            graph.remove(connector.getGroupName());
        } else {
            getLogger().info(String.format("Resource %s is already detached from the topology analyzer", resourceName));
        }
    }

    private void addNotificationListener(final NotificationSupport support){
        support.addNotificationListener(this, null, null);
    }

    private void removeNotificationListener(final NotificationSupport support) {
        try {
            support.removeNotificationListener(this);
        } catch (final ListenerNotFoundException e) {
            getLogger().log(Level.WARNING, "Unable to remove notification listener", e);
        }
    }

    @Override
    public void handleNotification(final Notification notification, final Object handback) {
        if (notification instanceof SpanNotification)
            graph.accept((SpanNotification) notification);
        else if (notification instanceof NotificationContainer)
            handleNotification(((NotificationContainer) notification).get(), handback);
    }

    @Override
    public void close() {
        graph.clear();
        getBundleContext().removeServiceListener(this);
    }
}
