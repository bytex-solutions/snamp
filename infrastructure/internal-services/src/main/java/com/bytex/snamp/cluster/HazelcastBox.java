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
        return distributedObject.get();
    }

    @Override
    public void set(final Object value) {
        distributedObject.set(value);
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
        } while (!distributedObject.compareAndSet(null, current));
        return current;
    }

    @Override
    public Object accumulateAndGet(final Object right, final BinaryOperator<Object> operator) {
        Object prev, next;
        do{
            next = operator.apply(prev = distributedObject.get(), right);
        } while (!distributedObject.compareAndSet(prev, next));
        return next;
    }

    @Override
    public Object updateAndGet(final UnaryOperator<Object> operator) {
        Object prev, next;
        do{
            next = operator.apply(prev = distributedObject.get());
        } while (!distributedObject.compareAndSet(prev, next));
        return next;
    }

    @Override
    public Object getAndSet(final Object newValue) {
        return distributedObject.getAndSet(newValue);
    }

    @Override
    public Object getOrDefault(final Supplier<?> defaultProvider) {
        final Object current = distributedObject.get();
        return current == null ? defaultProvider.get() : distributedObject.get();
    }

    /**
     * Determines whether this container has stored value.
     *
     * @return {@literal true}, if this container has stored value.
     */
    @Override
    public boolean hasValue() {
        return !distributedObject.isNull();
    }

    @Override
    public void accept(final Object value) {
        distributedObject.set(value);
    }

    static void destroy(final HazelcastInstance hazelcast, final String serviceName) {
        hazelcast.getAtomicReference(serviceName).destroy();
    }
}
