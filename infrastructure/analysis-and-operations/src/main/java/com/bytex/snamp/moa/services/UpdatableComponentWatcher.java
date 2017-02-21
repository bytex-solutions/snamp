package com.bytex.snamp.moa.services;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.WeakEventListenerList;
import com.bytex.snamp.connector.supervision.HealthStatus;
import com.bytex.snamp.moa.watching.*;

import javax.annotation.Nonnull;
import javax.management.Attribute;
import javax.management.JMException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class UpdatableComponentWatcher extends ConcurrentHashMap<String, AttributeChecker> implements ComponentWatcher {
    private static final long serialVersionUID = 6719849642864993362L;
    private static final AtomicReferenceFieldUpdater<UpdatableComponentWatcher, AbstractStatusDetails> STATUS_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(UpdatableComponentWatcher.class, AbstractStatusDetails.class, "status");

    private static final class StatusChangedEvent extends ComponentStatusChangedEvent {
        private static final long serialVersionUID = -6608026114593286031L;
        private final AbstractStatusDetails previousStatus;
        private final AbstractStatusDetails newStatus;

        private StatusChangedEvent(final UpdatableComponentWatcher source,
                                   final AbstractStatusDetails newStatus,
                                   final AbstractStatusDetails previousStatus) {
            super(source);
            this.previousStatus = Objects.requireNonNull(previousStatus);
            this.newStatus = Objects.requireNonNull(newStatus);
        }

        @Override
        public AbstractStatusDetails getStatusDetails() {
            return newStatus;
        }

        @Override
        public AbstractStatusDetails getPreviousStatusDetails() {
            return previousStatus;
        }
    }

    @SpecialUse(SpecialUse.Case.JVM)
    private volatile AbstractStatusDetails status;

    private final ConcurrentMap<String, HealthStatus> attributesStatusMap;
    private final WeakEventListenerList<ComponentStatusEventListener, ComponentStatusChangedEvent> listeners;

    UpdatableComponentWatcher() {
        super(15);
        status = OkStatusDetails.INSTANCE;
        attributesStatusMap = new ConcurrentHashMap<>(15);
        listeners = WeakEventListenerList.create(ComponentStatusEventListener::statusChanged);
    }

    @Override
    public void clear(){
        attributesStatusMap.clear();
        super.clear();
        listeners.clear();
        reset();
    }

    /**
     * Resets internal state of the object.
     */
    @Override
    public void reset() {
        STATUS_UPDATER.set(this, OkStatusDetails.INSTANCE);
    }

    /**
     * Gets status of the component.
     *
     * @return Status of the component.
     */
    @Override
    public AbstractStatusDetails getStatus() {
        return STATUS_UPDATER.get(this);
    }

    /**
     * Gets map of attribute checkers where key represents attribute name.
     *
     * @return Mutable map of attribute checkers.
     */
    @Override
    public ConcurrentMap<String, AttributeChecker> getAttributeCheckers() {
        return this;
    }

    @Override
    public AttributeChecker remove(@Nonnull final Object key) {
        attributesStatusMap.remove(key);
        return super.remove(key);
    }

    @Override
    public void addStatusEventListener(final ComponentStatusEventListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeStatusEventListener(final ComponentStatusEventListener listener) {
        listeners.remove(listener);
    }

    private void updateStatus(final AbstractStatusDetails newStatus) {
        AbstractStatusDetails prev, next;
        do {
            prev = STATUS_UPDATER.get(this);
            next = prev.replaceWith(newStatus);
            if (prev == next)   //status was not changed
                return;
        } while (!STATUS_UPDATER.compareAndSet(this, prev, next));

        listeners.fire(new StatusChangedEvent(this, next, prev));
    }

    void updateStatus(final String resourceName, final Attribute attribute) {
        final AttributeChecker checker = get(attribute.getName());
        if (checker != null) {
            final HealthStatus attributeStatus = checker.getStatus(attribute);
            attributesStatusMap.put(attribute.getName(), attributeStatus);
            final HealthStatus newStatus = attributesStatusMap.values().stream().reduce(HealthStatus.OK, HealthStatus::max);
            updateStatus(new CausedByAttributeStatusDetails(resourceName, attribute, newStatus));
        }
    }

    void updateStatus(final String resourceName, final JMException error) {
        //reset state of all attributes
        attributesStatusMap.replaceAll((attribute, old) -> HealthStatus.OK);
        updateStatus(new ResourceUnavailableStatus(resourceName, error));
    }

    void removeResource(final String resourceName) {
        STATUS_UPDATER.updateAndGet(this, existing -> existing.getResourceName().equals(resourceName) ? OkStatusDetails.INSTANCE : existing);
    }
}
