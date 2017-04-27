package com.bytex.snamp.supervision.def;

import com.bytex.snamp.AbstractWeakEventListenerList;
import com.bytex.snamp.WeakEventListener;
import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.attributes.AttributeSupport;
import com.bytex.snamp.connector.attributes.checkers.AttributeChecker;
import com.bytex.snamp.connector.health.*;
import com.bytex.snamp.supervision.health.HealthStatusChangedEvent;
import com.bytex.snamp.supervision.health.HealthStatusEventListener;
import com.bytex.snamp.supervision.health.HealthStatusProvider;
import com.bytex.snamp.supervision.health.ResourceGroupHealthStatus;
import org.osgi.framework.BundleContext;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.JMException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Represents default implementation of {@link HealthStatusProvider}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class DefaultHealthStatusProvider implements HealthStatusProvider, AutoCloseable {
    private final class DefaultHealthStatusChangedEvent extends HealthStatusChangedEvent {
        private static final long serialVersionUID = -6608026114593286031L;
        private final ResourceGroupHealthStatus previousStatus;
        private final ResourceGroupHealthStatus newStatus;

        private DefaultHealthStatusChangedEvent(@Nonnull final ResourceGroupHealthStatus newStatus,
                                   @Nonnull final ResourceGroupHealthStatus previousStatus) {
            super(DefaultHealthStatusProvider.this);
            this.previousStatus = previousStatus;
            this.newStatus = newStatus;
        }

        @Override
        public DefaultHealthStatusProvider getSource() {
            return DefaultHealthStatusProvider.this;
        }

        @Override
        public ResourceGroupHealthStatus getNewStatus() {
            return newStatus;
        }

        @Override
        public ResourceGroupHealthStatus getPreviousStatus() {
            return previousStatus;
        }
    }

    private static final class DefaultResourceGroupHealthStatus extends HashMap<String, HealthStatus> implements ResourceGroupHealthStatus{
        private static final long serialVersionUID = -411291568337973940L;
        private HealthStatus groupStatus;

        private DefaultResourceGroupHealthStatus(@Nonnull final HealthStatus groupStatus){
            this.groupStatus = Objects.requireNonNull(groupStatus);
        }

        private DefaultResourceGroupHealthStatus() {
            this(new OkStatus());
        }

        private DefaultResourceGroupHealthStatus(final DefaultResourceGroupHealthStatus proto){
            super(proto);
            groupStatus = proto.groupStatus;
        }

        @Override
        public HealthStatus getSummaryStatus() {
            return values().stream().reduce(HealthStatus::worst).orElse(groupStatus).worst(groupStatus);
        }

        boolean like(final DefaultResourceGroupHealthStatus other) {
            if (other.keySet().equals(keySet()) && other.groupStatus.like(groupStatus)) {
                for (final Entry<String, HealthStatus> thisEntry : entrySet())
                    if (!thisEntry.getValue().like(other.get(thisEntry.getKey())))
                        return false;
                return true;
            } else
                return false;
        }

        void setResourceStatus(final String resourceName,
                               final ManagedResourceConnector connector,
                               final Map<String, AttributeChecker> checkers) {
            //1. Using health check provided by connector itself
            HealthStatus newStatus = connector.queryObject(HealthCheckSupport.class).map(HealthCheckSupport::getStatus).orElseGet(OkStatus::new);
            if (!(newStatus instanceof OkStatus)) {
                put(resourceName, newStatus);
                return;
            }
            //2. read attributes from connector
            final AttributeList attributes;
            {
                final Optional<AttributeSupport> support = connector.queryObject(AttributeSupport.class);
                if (support.isPresent())
                    try {
                        attributes = support.get().getAttributes();
                    } catch (final JMException e) {
                        if (e.getCause() instanceof IOException)
                            put(resourceName, new ConnectionProblem(resourceName, (IOException) e.getCause()));
                        else
                            put(resourceName, new ResourceConnectorMalfunction(resourceName, e));
                        return;
                    }
                else
                    attributes = new AttributeList();
            }
            //3. update health status using attribute checkers
            for (final Attribute attribute : attributes.asList()) {
                final AttributeChecker checker = checkers.get(attribute.getName());
                if (checker != null)
                    newStatus = checker.getStatus(attribute).createStatus(resourceName, attribute).worst(newStatus);
            }
            put(resourceName, newStatus);
        }

        @Override
        public String toString() {
            return getSummaryStatus().toString();
        }
    }

    private static final class WeakHealthStatusEventListener extends WeakEventListener<HealthStatusEventListener, HealthStatusChangedEvent>{
        private final Object handback;

        WeakHealthStatusEventListener(@Nonnull final HealthStatusEventListener listener, final Object handback) {
            super(listener);
            this.handback = handback;
        }

        @Override
        protected void invoke(@Nonnull final HealthStatusEventListener listener, @Nonnull final HealthStatusChangedEvent event) {
            listener.statusChanged(event, handback);
        }
    }

    private static final class HealthStatusEventListenerList extends AbstractWeakEventListenerList<HealthStatusEventListener, HealthStatusChangedEvent>{
        void add(final HealthStatusEventListener listener, final Object handback) {
            add(listener, l -> new WeakHealthStatusEventListener(l, handback));
        }
    }

    private final ConcurrentMap<String, AttributeChecker> checkers;
    private final HealthStatusEventListenerList listeners;
    private volatile DefaultResourceGroupHealthStatus status;
    private HealthStatusTrigger trigger;

    /**
     * Initializes a new health status provider.
     */
    public DefaultHealthStatusProvider() {
        checkers = new ConcurrentHashMap<>();
        status = new DefaultResourceGroupHealthStatus();
        trigger = HealthStatusTrigger.IDENTITY;
        listeners = new HealthStatusEventListenerList();
    }

    /**
     * Assigns health status trigger.
     * @param value A new health status trigger. Cannot be {@literal null}.
     */
    public final void setTrigger(@Nonnull final HealthStatusTrigger value) {
        trigger = Objects.requireNonNull(value);
    }

    private void updateStatus(final DefaultResourceGroupHealthStatus newStatus) {
        final DefaultResourceGroupHealthStatus prevStatus;
        synchronized (this){    //trigger invocation should be enqueued
            prevStatus = status;
            if(prevStatus.like(newStatus))
                return; //status was not changed. Exit without any notifications.
            trigger.statusChanged(prevStatus, newStatus);
            if(prevStatus.like(newStatus))
                return; //status was not changed. Exit without any modifications.
            status = newStatus;
        }
        listeners.accept(new DefaultHealthStatusChangedEvent(newStatus, prevStatus));
    }

    protected final void updateStatus(final Map<String, ? extends ManagedResourceConnector> resources, final HealthStatus groupStatus) {
        final DefaultResourceGroupHealthStatus newStatus = new DefaultResourceGroupHealthStatus(groupStatus);
        resources.forEach((resourceName, connector) -> newStatus.setResourceStatus(resourceName, connector, checkers));
        updateStatus(newStatus);
    }

    protected final void updateStatus(final BundleContext context,
                                      final Set<String> resources,
                                      final HealthStatus groupStatus){
        final DefaultResourceGroupHealthStatus newStatus = new DefaultResourceGroupHealthStatus(groupStatus);
        for (final String resourceName : resources)
            ManagedResourceConnectorClient.tryCreate(context, resourceName).ifPresent(client -> {
                try {
                    newStatus.setResourceStatus(client.getManagedResourceName(), client, checkers);
                } finally {
                    client.close();
                }
            });
        updateStatus(newStatus);
    }

    protected final void removeResource(final String resourceName){
        final DefaultResourceGroupHealthStatus newStatus = new DefaultResourceGroupHealthStatus(status);
        newStatus.remove(resourceName);
        updateStatus(newStatus);
    }

    /**
     * Adds checker for the specified attribute.
     * @param attributeName Name of the attribute to check.
     * @param checker Attribute checker implementation. Cannot be {@literal null}.
     */
    public final void addChecker(final String attributeName, @Nonnull final AttributeChecker checker){
        checkers.put(attributeName, Objects.requireNonNull(checker));
    }

    /**
     * Removes checker for the specified attribute.
     * @param attributeName Name of the attribute.
     * @return Removed attribute checker.
     */
    public final AttributeChecker removeChecker(final String attributeName){
        return checkers.remove(attributeName);
    }

    /**
     * Removes all attribute checkers.
     */
    public final void removeCheckers(){
        checkers.clear();
    }

    /**
     * Resets internal state of the object.
     */
    @Override
    public synchronized void reset() {
        status = new DefaultResourceGroupHealthStatus();
    }

    /**
     * Adds listener of health status.
     *
     * @param listener Listener of health status to add.
     * @param handback Handback object that will be returned into listener.
     */
    @Override
    public final void addHealthStatusEventListener(@Nonnull final HealthStatusEventListener listener, final Object handback) {
        listeners.add(listener, handback);
    }

    /**
     * Adds listener of health status.
     *
     * @param listener Listener of health status to add.
     */
    @Override
    public final void addHealthStatusEventListener(@Nonnull final HealthStatusEventListener listener) {
        addHealthStatusEventListener(listener, null);
    }

    /**
     * Determines whether the connected managed resource is alive.
     *
     * @return Status of the remove managed resource.
     */
    @Override
    @Nonnull
    public final ResourceGroupHealthStatus getStatus() {
        return status;
    }

    /**
     * Removes listener of health status.
     *
     * @param listener Listener of health status to remove.
     */
    @Override
    public final void removeHealthStatusEventListener(@Nonnull final HealthStatusEventListener listener) {
        listeners.remove(listener);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void close() throws Exception {
        listeners.clear();
        checkers.clear();
    }
}
