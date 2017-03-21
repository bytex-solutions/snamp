package com.bytex.snamp.moa.services;

import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.Stateful;
import com.bytex.snamp.configuration.ManagedResourceGroupWatcherConfiguration;
import com.bytex.snamp.configuration.ScriptletConfiguration;
import com.bytex.snamp.connector.attributes.checkers.AttributeChecker;
import com.bytex.snamp.connector.attributes.checkers.AttributeCheckerFactory;
import com.bytex.snamp.connector.attributes.checkers.InvalidAttributeCheckerException;
import com.bytex.snamp.connector.supervision.*;
import com.bytex.snamp.connector.supervision.triggers.HealthStatusTrigger;
import com.bytex.snamp.connector.supervision.triggers.InvalidTriggerException;
import com.bytex.snamp.connector.supervision.triggers.TriggerFactory;
import com.bytex.snamp.core.DistributedServices;
import com.bytex.snamp.internal.Utils;
import org.osgi.framework.BundleContext;

import javax.management.Attribute;
import javax.management.JMException;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class UpdatableGroupWatcher extends WeakReference<HealthStatusEventListener> implements Stateful, SafeCloseable {
    private static final AttributeCheckerFactory CHECKER_FACTORY = new AttributeCheckerFactory();
    private static final TriggerFactory TRIGGER_FACTORY = new TriggerFactory();

    private static final class StatusChangedEvent extends HealthStatusChangedEvent {
        private static final long serialVersionUID = -6608026114593286031L;
        private final HealthStatus previousStatus;
        private final HealthStatus newStatus;

        private StatusChangedEvent(final UpdatableGroupWatcher source,
                                   final HealthStatus newStatus,
                                   final HealthStatus previousStatus) {
            super(source);
            this.previousStatus = Objects.requireNonNull(previousStatus);
            this.newStatus = Objects.requireNonNull(newStatus);
        }

        @Override
        public StatusChangedEvent getSource() {
            return (StatusChangedEvent) super.getSource();
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

    private volatile HealthStatus status;
    private final ConcurrentMap<String, AttributeChecker> attributeCheckers;
    private final HealthStatusTrigger trigger;

    UpdatableGroupWatcher(final ManagedResourceGroupWatcherConfiguration configuration,
                          final HealthStatusEventListener statusListener) throws InvalidAttributeCheckerException, InvalidTriggerException {
        super(statusListener);
        status = new OkStatus();
        this.attributeCheckers = new ConcurrentHashMap<>(15);
        for (final Map.Entry<String, ? extends ScriptletConfiguration> checker : configuration.getAttributeCheckers().entrySet()) {
            attributeCheckers.put(checker.getKey(), CHECKER_FACTORY.createChecker(checker.getValue()));
        }
        this.trigger = TRIGGER_FACTORY.createTrigger(configuration.getTrigger());
    }

    @Override
    public void close() {
        clear();
        attributeCheckers.clear();
    }

    /**
     * Resets internal state of the object.
     */
    @Override
    public synchronized void reset() {
        status = new OkStatus();
    }

    /**
     * Gets status of the component.
     *
     * @return Status of the component.
     */
    HealthStatus getStatus() {
        return status;
    }

    private BundleContext getBundleContext(){
        return Utils.getBundleContextOfObject(this);
    }

    private HealthStatus invokeTrigger(final HealthStatus prev, final HealthStatus next){
        //trigger can be executed on active node only
        return DistributedServices.isActiveNode(getBundleContext()) ? trigger.statusChanged(prev, next) : next;
    }

    private void updateStatus(final Function<? super HealthStatus, ? extends HealthStatus> statusUpdater) {
        final HealthStatus newStatus, prevStatus;
        synchronized (this) {   //calling of the trigger should be enqueued
            final HealthStatus tempNewStatus = statusUpdater.apply(prevStatus = status);
            if (tempNewStatus.compareTo(prevStatus) == 0)
                return;
            newStatus = invokeTrigger(prevStatus, tempNewStatus);
            if (newStatus.compareTo(prevStatus) == 0)

                return;
            status = newStatus;
        }
        final HealthStatusEventListener listener = get();
        if (listener != null)
            listener.statusChanged(new StatusChangedEvent(this, newStatus, prevStatus));
    }

    private void updateStatus(final HealthStatus newStatus) {
        updateStatus(existing -> newStatus);
    }

    void updateStatus(final String resourceName, final Iterable<? extends Attribute> attributes) {
        HealthStatus newStatus = new OkStatus(resourceName);
        for (final Attribute attribute : attributes) {
            final AttributeChecker checker = attributeCheckers.get(attribute.getName());
            if (checker != null)
                newStatus = checker.getStatus(attribute).createStatus(resourceName, attribute).combine(newStatus);
        }
        updateStatus(newStatus);
    }

    void updateStatus(final String resourceName, final JMException error) {
        updateStatus(new ResourceIsNotAvailable(resourceName, error));
    }

    void removeResource(final String resourceName) {
        updateStatus(existing -> existing.getResourceName().equals(resourceName) ? new OkStatus(resourceName) : existing);
    }
}

