package com.bytex.snamp.moa.services;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.Aggregator;
import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.connector.AbstractManagedResourceTracker;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.ManagedResourceFilterBuilder;
import com.bytex.snamp.connector.notifications.NotificationContainer;
import com.bytex.snamp.connector.notifications.NotificationSupport;
import com.bytex.snamp.instrumentation.measurements.jmx.SpanNotification;
import com.bytex.snamp.moa.topology.ComponentVertex;
import com.bytex.snamp.moa.topology.TopologyAnalyzer;

import javax.annotation.Nonnull;
import javax.management.ListenerNotFoundException;
import javax.management.Notification;
import javax.management.NotificationListener;
import java.util.logging.Level;

/**
 * Represents implementation of topology analyzer.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class DefaultTopologyAnalyzer extends AbstractManagedResourceTracker implements TopologyAnalyzer, SafeCloseable, NotificationListener {
    private final FilteredGraphOfComponents graph;

    DefaultTopologyAnalyzer(final long historySize){
        graph = new FilteredGraphOfComponents(historySize);
        createResourceFilter().addServiceListener(getBundleContext(), this);
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

    @Override
    protected void addResource(final ManagedResourceConnectorClient connector) {
        final String resourceName = connector.getManagedResourceName();
        if (trackedResources.contains(resourceName)) {
            getLogger().info(String.format("Resource %s is already attached to the topology analyzer", resourceName));
        } else {
            graph.add(connector.getGroupName());
            Aggregator.queryAndAccept(connector, NotificationSupport.class, this::addNotificationListener);
        }
    }

    @Override
    protected void removeResource(final ManagedResourceConnectorClient connector) {
        final String resourceName = connector.getManagedResourceName();
        if (trackedResources.contains(resourceName)) {
            Aggregator.queryAndAccept(connector, NotificationSupport.class, this::removeNotificationListener);
            graph.remove(connector.getGroupName());
        } else {
            getLogger().info(String.format("Resource %s is already detached from the topology analyzer", resourceName));
        }
    }
    
    @Nonnull
    @Override
    protected ManagedResourceFilterBuilder createResourceFilter() {
        return ManagedResourceConnectorClient.filterBuilder();
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
