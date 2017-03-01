package com.bytex.snamp.moa.services;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.Stateful;
import com.bytex.snamp.configuration.ManagedResourceGroupWatcherConfiguration;
import com.bytex.snamp.connector.attributes.checkers.AttributeCheckStatus;
import com.bytex.snamp.connector.attributes.checkers.AttributeChecker;
import com.bytex.snamp.connector.attributes.checkers.AttributeCheckerFactory;
import com.bytex.snamp.connector.attributes.checkers.InvalidAttributeCheckerException;
import com.bytex.snamp.connector.supervision.*;
import com.bytex.snamp.connector.supervision.triggers.TriggerFactory;
import com.bytex.snamp.core.LoggerProvider;
import com.google.common.collect.ImmutableMap;

import javax.management.Attribute;
import javax.management.JMException;
import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class UpdatableGroupWatcher extends WeakReference<GroupStatusEventListener> implements Stateful {
    private static final AtomicReferenceFieldUpdater<UpdatableGroupWatcher, HealthStatus> STATUS_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(UpdatableGroupWatcher.class, HealthStatus.class, "status");
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

    @SpecialUse(SpecialUse.Case.JVM)
    private volatile HealthStatus status;

    private final ConcurrentMap<String, AttributeCheckStatus> attributesStatusMap;
    private final ImmutableMap<String, AttributeChecker> attributeCheckers;

    UpdatableGroupWatcher(final ManagedResourceGroupWatcherConfiguration configuration,
                          final GroupStatusEventListener statusListener) {
        super(statusListener);
        status = OK_STATUS;
        attributesStatusMap = new ConcurrentHashMap<>(15);
        final ImmutableMap.Builder<String, AttributeChecker> attributeCheckers = ImmutableMap.builder();
        final Logger logger = getLogger();
        configuration.getAttributeCheckers().forEach((attributeName, checkerCode) -> {
            final AttributeChecker checker;
            try {
                checker = CHECKER_FACTORY.createChecker(checkerCode);
            } catch (final InvalidAttributeCheckerException e) {
                logger.log(Level.WARNING, "Unable to parse checker for attribute " + attributeName, e);
                return;
            }
            attributeCheckers.put(attributeName, checker);
        });
        this.attributeCheckers = attributeCheckers.build();
    }

    private Logger getLogger(){
        return LoggerProvider.getLoggerForObject(this);
    }

    @Override
    public void clear(){
        super.clear();
        attributesStatusMap.clear();
        reset();
    }

    /**
     * Resets internal state of the object.
     */
    @Override
    public void reset() {
        STATUS_UPDATER.set(this, OK_STATUS);
    }

    /**
     * Gets status of the component.
     *
     * @return Status of the component.
     */
    HealthStatus getStatus() {
        return STATUS_UPDATER.get(this);
    }

    private void updateStatus(final HealthStatus newStatus) {
        HealthStatus prev, next;
        do {
            prev = STATUS_UPDATER.get(this);
            next = prev.combine(newStatus);
            if (prev == next)   //status was not changed
                return;
        } while (!STATUS_UPDATER.compareAndSet(this, prev, next));
        final GroupStatusEventListener listener = get();
        if (listener != null)
            listener.statusChanged(new StatusChangedEvent(this, next, prev));
    }

    void updateStatus(final String resourceName, final Attribute attribute) {
        final AttributeChecker checker = attributeCheckers.get(attribute.getName());
        if (checker != null) {
            final AttributeCheckStatus attributeStatus = checker.getStatus(attribute);
            attributesStatusMap.put(attribute.getName(), attributeStatus);
            final AttributeCheckStatus newStatus = attributesStatusMap.values().stream().reduce(AttributeCheckStatus.OK, AttributeCheckStatus::max);
            updateStatus(newStatus.createStatus(resourceName, attribute));
        }
    }

    void updateStatus(final String resourceName, final JMException error) {
        //reset state of all attributes
        attributesStatusMap.replaceAll((attribute, old) -> AttributeCheckStatus.OK);
        updateStatus(new ResourceInGroupIsNotUnavailable(resourceName, error));
    }

    void removeResource(final String resourceName) {
        STATUS_UPDATER.updateAndGet(this, existing -> existing.getResourceName().equals(resourceName) ? OK_STATUS : existing);
    }

    void removeAttribute(final String attributeName) {
        attributesStatusMap.remove(attributeName);
        STATUS_UPDATER.updateAndGet(this, existing -> existing instanceof InvalidAttributeValue && ((InvalidAttributeValue) existing).getAttribute().getName().equals(attributeName) ? OK_STATUS : existing);
    }
}
