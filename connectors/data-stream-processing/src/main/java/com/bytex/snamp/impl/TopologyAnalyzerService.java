package com.bytex.snamp.impl;

import com.bytex.snamp.Aggregator;
import com.bytex.snamp.MapUtils;
import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.concurrent.LockManager;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.connector.ClusteredResourceConnector;
import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.dsp.notifications.SpanNotification;
import com.bytex.snamp.connector.notifications.NotificationSupport;
import com.bytex.snamp.core.LoggerProvider;
import com.bytex.snamp.instrumentation.measurements.Span;
import com.bytex.snamp.moa.topology.ComponentVertex;
import com.bytex.snamp.moa.topology.GraphOfComponents;
import com.bytex.snamp.moa.topology.TopologyAnalyzer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

import javax.management.ListenerNotFoundException;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;
import static com.bytex.snamp.internal.Utils.isInstanceOf;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class TopologyAnalyzerService extends GraphOfComponents implements TopologyAnalyzer, ServiceListener, SafeCloseable, NotificationListener, NotificationFilter {
    private static final String HISTORY_SIZE_PARAM = "topologyAnalyzerHistorySize";
    private static final long DEFAULT_HISTORY_SIZE = 10_000L;
    private final Map<String, Long> allowedComponents;//key - component name, value - number of components with the same name
    private final ReadWriteLock lock;

    TopologyAnalyzerService(final ConfigurationManager configManager) throws IOException {
        super(configManager.transformConfiguration(config -> MapUtils.getValueAsLong(config, HISTORY_SIZE_PARAM, Long::parseLong).orElse(DEFAULT_HISTORY_SIZE)));
        ManagedResourceConnectorClient.addResourceListener(getBundleContext(), this);
        allowedComponents = new HashMap<>(15);
        lock = new ReentrantReadWriteLock();
    }

    void init(){
        for(final ServiceReference<ManagedResourceConnector> connectorRef: ManagedResourceConnectorClient.getConnectors(getBundleContext()).values()){
            serviceChanged(ServiceEvent.REGISTERED, connectorRef);
        }
    }

    @Override
    public void visit(final Visitor visitor) {
        for (final ComponentVertex vertex : values())
            if (!visitor.visit(vertex))
                return;
    }

    private BundleContext getBundleContext(){
        return getBundleContextOfObject(this);
    }

    private static boolean filterSpan(final Map<String, ?> allowedComponents, final Span span){
        return allowedComponents.containsKey(span.getComponentName());
    }

    @Override
    protected boolean filterSpan(final Span span) {
        return LockManager
                .lockAndApply(lock.readLock(), allowedComponents, span, TopologyAnalyzerService::filterSpan)
                .orElse(Boolean.FALSE);
    }

    private static void addComponent(final Map<String, Long> allowedComponents, final ClusteredResourceConnector resource) {
        allowedComponents.compute(resource.getComponentName(), (k, v) -> v == null ? 0L : v + 1L);
    }

    private void resourceRegistered(final ClusteredResourceConnector resource) {
        LockManager.lockAndAccept(lock.writeLock(), allowedComponents, resource, TopologyAnalyzerService::addComponent);
    }

    private boolean removeComponent(final ClusteredResourceConnector resource) {
        return allowedComponents.compute(resource.getComponentName(), (k, v) -> {
            if (v == null)
                return null;
            v = v - 1L;
            return v <= 1L ? null : v;
        }) != null;
    }

    private void resourceUnregistered(final ClusteredResourceConnector resource) {
        LockManager.lockAndApply(lock.writeLock(), this, resource, TopologyAnalyzerService::removeComponent);
    }

    private void addNotificationListener(final NotificationSupport notifications){
        notifications.addNotificationListener(this, this, null);
    }

    private void removeNotificationListener(final NotificationSupport notifications) {
        try {
            notifications.removeNotificationListener(this);
        } catch (final ListenerNotFoundException e) {
            getLogger().log(Level.WARNING, "Unable to remove notification listener", e);
        }
    }

    private Logger getLogger(){
        return LoggerProvider.getLoggerForBundle(getBundleContext());
    }

    private void serviceChanged(final int type, final ServiceReference<ManagedResourceConnector> connectorRef){
        @SuppressWarnings("unchecked")
        final ManagedResourceConnectorClient client = new ManagedResourceConnectorClient(getBundleContext(), connectorRef);
        try {
            switch (type) {
                case ServiceEvent.REGISTERED:
                    Aggregator.queryAndAccept(client, ClusteredResourceConnector.class, this::resourceRegistered);
                    Aggregator.queryAndAccept(client, NotificationSupport.class, this::addNotificationListener);
                    return;
                case ServiceEvent.MODIFIED_ENDMATCH:
                case ServiceEvent.UNREGISTERING:
                    Aggregator.queryAndAccept(client, NotificationSupport.class, this::removeNotificationListener);
                    Aggregator.queryAndAccept(client, ClusteredResourceConnector.class, this::resourceUnregistered);
                    return;
            }
        } finally {
            client.release(getBundleContext());
        }
    }

    /**
     * Receives notification that a service has had a lifecycle change.
     *
     * @param event The {@code ServiceEvent} object.
     */
    @Override
    public void serviceChanged(final ServiceEvent event) {
        if (isInstanceOf(event.getServiceReference(), ManagedResourceConnector.class))
            serviceChanged(event.getType(), (ServiceReference<ManagedResourceConnector>) event.getServiceReference());
    }

    @Override
    public boolean isNotificationEnabled(final Notification notification) {
        return notification instanceof SpanNotification;
    }

    @Override
    public void handleNotification(final Notification notification, final Object handback) {
        if (isNotificationEnabled(notification))
            handleNotification((SpanNotification) notification);
    }

    /**
     * Releases all resources associated with this object.
     */
    @Override
    public void close() {
        getBundleContext().removeServiceListener(this);
        clear();
    }
}
