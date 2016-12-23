package com.bytex.snamp.cluster;

import com.bytex.snamp.Box;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IAtomicReference;

import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Represents distributed Box service.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class HazelcastBox extends HazelcastSharedObject<IAtomicReference<Object>> implements Box<Object> {

    HazelcastBox(final HazelcastInstance hazelcast, final String boxName){
        super(hazelcast, boxName, HazelcastInstance::getAtomicReference);
    }

    @Override
    public Object get() {
        return getDistributedObject().get();
    }

    @Override
    public void set(final Object value) {
        getDistributedObject().set(value);
    }

    @Override
    public Object setIfAbsent(final Supplier<?> valueProvider) {
        Object current;
        do {
            current = get();
            if (current == null)
                current = valueProvider.get();
            else
                break;
        } while (!getDistributedObject().compareAndSet(null, current));
        return current;
    }

    @Override
    public Object accumulateAndGet(final Object right, final BinaryOperator<Object> operator) {
        Object prev, next;
        do{
            next = operator.apply(prev = getDistributedObject().get(), right);
        } while (!getDistributedObject().compareAndSet(prev, next));
        return next;
    }

    @Override
    public Object updateAndGet(final UnaryOperator<Object> operator) {
        Object prev, next;
        do{
            next = operator.apply(prev = getDistributedObject().get());
        } while (!getDistributedObject().compareAndSet(prev, next));
        return next;
    }

    @Override
    public Object getAndSet(final Object newValue) {
        return getDistributedObject().getAndSet(newValue);
    }

    @Override
    public Object getOrDefault(final Supplier<?> defaultProvider) {
        final Object current = getDistributedObject().get();
        return current == null ? defaultProvider.get() : getDistributedObject().get();
    }

    /**
     * Determines whether this container has stored value.
     *
     * @return {@literal true}, if this container has stored value.
     */
    @Override
    public boolean hasValue() {
        return !getDistributedObject().isNull();
    }

    @Override
    public void accept(final Object value) {
        getDistributedObject().set(value);
    }
}
