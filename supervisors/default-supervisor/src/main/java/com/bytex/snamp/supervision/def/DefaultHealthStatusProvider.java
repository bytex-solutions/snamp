package com.bytex.snamp.supervision.def;

import com.bytex.snamp.WeakEventListenerList;
import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.connector.attributes.AttributeSupport;
import com.bytex.snamp.connector.attributes.checkers.AttributeChecker;
import com.bytex.snamp.connector.health.HealthCheckSupport;
import com.bytex.snamp.connector.health.HealthStatus;
import com.bytex.snamp.connector.health.OkStatus;
import com.bytex.snamp.connector.health.ResourceIsNotAvailable;
import com.bytex.snamp.connector.health.triggers.HealthStatusTrigger;
import com.bytex.snamp.core.DistributedServices;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.supervision.health.HealthStatusChangedEvent;
import com.bytex.snamp.supervision.health.HealthStatusEventListener;
import com.bytex.snamp.supervision.health.HealthStatusProvider;
import org.osgi.framework.BundleContext;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.JMException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * Represents default implementation of {@link HealthStatusProvider}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class DefaultHealthStatusProvider implements HealthStatusProvider, AutoCloseable {
    private final class DefaultHealthStatusChangedEvent extends HealthStatusChangedEvent {
        private static final long serialVersionUID = -6608026114593286031L;
        private final HealthStatus previousStatus;
        private final HealthStatus newStatus;

        private DefaultHealthStatusChangedEvent(@Nonnull final HealthStatus newStatus,
                                   @Nonnull final HealthStatus previousStatus) {
            super(DefaultHealthStatusProvider.this);
            this.previousStatus = previousStatus;
            this.newStatus = newStatus;
        }

        @Override
        public String getGroupName() {
            return DefaultHealthStatusProvider.this.getGroupName();
        }

        @Override
        public DefaultHealthStatusProvider getSource() {
            return DefaultHealthStatusProvider.this;
        }

        @Override
        public HealthStatus getNewStatus() {
            return newStatus;
        }

        @Override
        public HealthStatus getPreviousStatus() {
            return previousStatus;
        }
    }

    private final ConcurrentMap<String, AttributeChecker> checkers;
    private final WeakEventListenerList<HealthStatusEventListener, HealthStatusChangedEvent> listeners;
    private volatile HealthStatus status;
    private HealthStatusTrigger trigger;
    private final String groupName;

    public DefaultHealthStatusProvider(@Nonnull final String groupName) {
        checkers = new ConcurrentHashMap<>();
        status = new OkStatus();
        trigger = HealthStatusTrigger.IDENTITY;
        listeners = WeakEventListenerList.create(HealthStatusEventListener::statusChanged);
        this.groupName = groupName;
    }

    public final String getGroupName(){
        return groupName;
    }

    public final void setTrigger(@Nonnull final HealthStatusTrigger value){
        trigger = value;
    }

    private HealthStatus invokeTrigger(final HealthStatus prev, final HealthStatus next){
        //trigger can be executed on active node only
        return DistributedServices.isActiveNode(getBundleContext()) ? trigger.statusChanged(prev, next) : next;
    }

    protected final HealthStatus updateStatus(final Function<? super HealthStatus, ? extends HealthStatus> statusUpdater) {
        final HealthStatus newStatus, prevStatus;
        synchronized (this) {   //calling of the trigger should be enqueued
            final HealthStatus tempNewStatus = statusUpdater.apply(prevStatus = status);
            if (tempNewStatus.compareTo(prevStatus) == 0)
                return prevStatus;
            newStatus = invokeTrigger(prevStatus, tempNewStatus);
            if (newStatus.compareTo(prevStatus) == 0)
                return prevStatus;
            status = newStatus;
        }
        listeners.fire(new DefaultHealthStatusChangedEvent(newStatus, prevStatus));
        return newStatus;
    }

    protected final HealthStatus updateStatus(final HealthStatus newStatus) {
        return updateStatus(existing -> newStatus);
    }

    private HealthStatus updateStatus(final String resourceName, final JMException error) {
        return updateStatus(new ResourceIsNotAvailable(resourceName, error));
    }

    public HealthStatus updateStatus(final String resourceName, final ManagedResourceConnector connector) {
        //1. Using health check provided by connector itself
        HealthStatus newStatus = connector.queryObject(HealthCheckSupport.class).map(HealthCheckSupport::getStatus)
                .orElseGet(() -> new OkStatus(resourceName));
        if (!(newStatus instanceof OkStatus))
            return updateStatus(newStatus);
        //2. read attributes from connector
        final AttributeList attributes;
        {
            final Optional<AttributeSupport> support = connector.queryObject(AttributeSupport.class);
            if (support.isPresent())
                try {
                    attributes = support.get().getAttributes();
                } catch (final JMException e) {
                    return updateStatus(resourceName, e);
                }
            else
                attributes = new AttributeList();
        }
        //3. update health status using attribute checkers
        for (final Attribute attribute : attributes.asList()) {
            final AttributeChecker checker = checkers.get(attribute.getName());
            if (checker != null)
                newStatus = checker.getStatus(attribute).createStatus(resourceName, attribute).combine(newStatus);
        }
        return updateStatus(newStatus);
    }

    public void removeResource(final String resourceName) {
        updateStatus(existing -> existing.getResourceName().equals(resourceName) ? new OkStatus(resourceName) : existing);
    }

    public final void addChecker(final String attributeName, final AttributeChecker checker){
        checkers.put(attributeName, checker);
    }

    public final void removeCheckers(){
        checkers.clear();
    }

    private BundleContext getBundleContext(){
        return Utils.getBundleContextOfObject(this);
    }

    /**
     * Resets internal state of the object.
     */
    @Override
    public void reset() {
        status = new OkStatus();
    }

    /**
     * Adds listener of health status.
     *
     * @param listener Listener of health status to add.
     */
    @Override
    public final void addHealthStatusEventListener(final HealthStatusEventListener listener) {
        listeners.add(listener);
    }

    /**
     * Determines whether the connected managed resource is alive.
     *
     * @return Status of the remove managed resource.
     */
    @Override
    public final HealthStatus getStatus() {
        return status;
    }

    /**
     * Removes listener of health status.
     *
     * @param listener Listener of health status to remove.
     */
    @Override
    public final void removeHealthStatusEventListener(final HealthStatusEventListener listener) {
        listeners.remove(listener);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void close() throws Exception {
        listeners.clear();
        checkers.clear();
    }
}
