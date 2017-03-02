package com.bytex.snamp.moa.services;

import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.Stateful;
import com.bytex.snamp.configuration.ManagedResourceGroupWatcherConfiguration;
import com.bytex.snamp.configuration.ScriptletConfiguration;
import com.bytex.snamp.connector.attributes.checkers.AttributeCheckStatus;
import com.bytex.snamp.connector.attributes.checkers.AttributeChecker;
import com.bytex.snamp.connector.attributes.checkers.AttributeCheckerFactory;
import com.bytex.snamp.connector.attributes.checkers.InvalidAttributeCheckerException;
import com.bytex.snamp.connector.supervision.*;
import com.bytex.snamp.connector.supervision.triggers.HealthStatusTrigger;
import com.bytex.snamp.connector.supervision.triggers.InvalidTriggerException;
import com.bytex.snamp.connector.supervision.triggers.TriggerFactory;
import com.bytex.snamp.core.LoggerProvider;
import com.google.common.collect.ImmutableMap;

import javax.management.Attribute;
import javax.management.JMException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class UpdatableGroupWatcher extends WeakReference<GroupStatusEventListener> implements Stateful, SafeCloseable {
    private static final OkStatus OK_STATUS = new OkStatus();
    private static final AttributeCheckerFactory CHECKER_FACTORY = new AttributeCheckerFactory();
    private static final TriggerFactory TRIGGER_FACTORY = new TriggerFactory();

    private static final class StatusChangedEvent extends GroupStatusChangedEvent {
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
    private final ConcurrentMap<String, AttributeCheckStatus> attributesStatusMap;
    private final ConcurrentMap<String, AttributeChecker> attributeCheckers;
    private final HealthStatusTrigger trigger;

    UpdatableGroupWatcher(final ManagedResourceGroupWatcherConfiguration configuration,
                          final GroupStatusEventListener statusListener) throws InvalidAttributeCheckerException, InvalidTriggerException {
        super(statusListener);
        status = OK_STATUS;
        attributesStatusMap = new ConcurrentHashMap<>(15);
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
        attributesStatusMap.clear();
        reset();
    }

    /**
     * Resets internal state of the object.
     */
    @Override
    public void reset() {
        updateStatus(existing -> OK_STATUS);
    }

    /**
     * Gets status of the component.
     *
     * @return Status of the component.
     */
    HealthStatus getStatus() {
        return status;
    }

    private void updateStatus(final Function<? super HealthStatus, ? extends HealthStatus> statusUpdater) {
        final HealthStatus newStatus, prevStatus;
        synchronized (this) {   //calling of the trigger should be enqueued
            final HealthStatus tempNewStatus = statusUpdater.apply(prevStatus = status);
            if (tempNewStatus.equals(prevStatus)) return;
            newStatus = trigger.statusChanged(prevStatus, tempNewStatus);
            if (newStatus.equals(prevStatus)) return;
            status = newStatus;
        }
        final GroupStatusEventListener listener = get();
        if (listener != null)
            listener.statusChanged(new StatusChangedEvent(this, newStatus, prevStatus));
    }

    void updateStatus(final String resourceName, final Attribute attribute) {
        final AttributeChecker checker = attributeCheckers.get(attribute.getName());
        if (checker != null) {
            final AttributeCheckStatus attributeStatus = checker.getStatus(attribute);
            attributesStatusMap.put(attribute.getName(), attributeStatus);
            final AttributeCheckStatus newStatus = attributesStatusMap.values().stream().reduce(AttributeCheckStatus.OK, AttributeCheckStatus::max);
            updateStatus(existing -> newStatus.createStatus(resourceName, attribute));
        }
    }

    void updateStatus(final String resourceName, final JMException error) {
        //reset state of all attributes
        attributesStatusMap.replaceAll((attribute, old) -> AttributeCheckStatus.OK);
        updateStatus(existing -> new ResourceInGroupIsNotUnavailable(resourceName, error));
    }

    void removeResource(final String resourceName) {
        updateStatus(existing -> existing.getResourceName().equals(resourceName) ? OK_STATUS : existing);
    }

    void removeAttribute(final String attributeName) {
        attributesStatusMap.remove(attributeName);
        updateStatus(existing -> existing instanceof InvalidAttributeValue && ((InvalidAttributeValue) existing).getAttribute().getName().equals(attributeName) ? OK_STATUS : existing);
    }
}
