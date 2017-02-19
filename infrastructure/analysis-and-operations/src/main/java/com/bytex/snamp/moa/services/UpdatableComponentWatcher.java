package com.bytex.snamp.moa.services;

import com.bytex.snamp.connector.health.HealthCheckStatus;
import com.bytex.snamp.moa.watching.AttributeChecker;
import com.bytex.snamp.moa.watching.ComponentStatusEventListener;
import com.bytex.snamp.moa.watching.ComponentWatcher;
import com.bytex.snamp.moa.watching.HealthCheckStatusDetails;

import javax.management.Attribute;
import javax.management.JMException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class UpdatableComponentWatcher extends AtomicReference<AbstractStatusDetails<?>> implements ComponentWatcher {
    private static final long serialVersionUID = 6719849642864993362L;
    private final Map<String, HealthCheckStatus> attributesStatusMap;
    private final ConcurrentMap<String, AttributeChecker> attributeCheckers;

    UpdatableComponentWatcher(){
        super(OkStatusDetails.INSTANCE);
        attributesStatusMap = new HashMap<>(15);
        attributeCheckers = new ConcurrentHashMap<>(15);
    }

    void clear(){
        synchronized (attributesStatusMap){
            attributesStatusMap.clear();
        }
        reset();
    }

    /**
     * Resets internal state of the object.
     */
    @Override
    public void reset() {
        set(OkStatusDetails.INSTANCE);
    }

    /**
     * Gets status of the component.
     *
     * @return Status of the component.
     */
    @Override
    public HealthCheckStatusDetails getStatus() {
        return get();
    }

    /**
     * Gets map of attribute checkers where key represents attribute name.
     *
     * @return Mutable map of attribute checkers.
     */
    @Override
    public ConcurrentMap<String, AttributeChecker> getAttributeCheckers() {
        return attributeCheckers;
    }

    @Override
    public void addStatusEventListener(final ComponentStatusEventListener listener) {

    }

    @Override
    public void removeStatusEventListener(final ComponentStatusEventListener listener) {

    }

    private void updateStatus(final AbstractStatusDetails<?> newStatus){
        updateAndGet(existing -> existing.replaceWith(newStatus));
    }

    private HealthCheckStatus reduceStatus(){
        return attributesStatusMap.values().stream().reduce(HealthCheckStatus.OK, HealthCheckStatus::max);
    }

    void updateStatus(final String resourceName, final Attribute attribute) {
        final AttributeChecker checker = attributeCheckers.getOrDefault(attribute.getName(), AttributeChecker.OK);
        final HealthCheckStatus attributeStatus = checker.getStatus(attribute);
        final HealthCheckStatus newStatus;
        synchronized (attributesStatusMap) {
            attributesStatusMap.put(attribute.getName(), attributeStatus);
            newStatus = reduceStatus();
        }
        updateStatus(new CausedByAttributeStatusDetails(resourceName, attribute, newStatus));
    }

    void updateStatus(final String resourceName, final JMException error) {
        updateStatus(new ResourceUnavailableStatus(resourceName, error));
    }

    void removeAttribute(final String resourceName, final String attributeName) {
        synchronized (attributesStatusMap) {
            attributesStatusMap.remove(attributeName);
        }
    }

    void removeResource(final String resourceName){
        updateAndGet(existing -> existing.getResourceName().equals(resourceName) ? OkStatusDetails.INSTANCE : existing);
    }
}
